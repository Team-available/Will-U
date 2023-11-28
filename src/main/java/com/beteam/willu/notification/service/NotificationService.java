package com.beteam.willu.notification.service;

import com.beteam.willu.notification.dto.NotificationRequestDto;
import com.beteam.willu.notification.dto.NotificationResponseDto;
import com.beteam.willu.notification.entity.Notification;
import com.beteam.willu.notification.entity.NotificationType;
import com.beteam.willu.notification.repository.EmitterRepository;
import com.beteam.willu.notification.repository.NotificationRepository;
import com.beteam.willu.post.entity.Post;
import com.beteam.willu.post.repository.PostRepository;
import com.beteam.willu.user.entity.User;
import com.beteam.willu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private static final Long DEFAULT_TIMEOUT = 10 * 60L * 1000; //테스트를 위해
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    //참가 요청 알림 보내기,거절알림 보내기
    public void sendRequestNotification(NotificationRequestDto requestDto, User loginUser) {
        Long postId = requestDto.getPostId();
        Post post = postRepository.findById(postId).orElseThrow();
        String type = requestDto.getType().getDescription();
        switch (type) {
            case "참가 신청" -> {
                log.info("참가 신청 알림");
                send(loginUser, post.getUser(), requestDto.getType(),
                        loginUser.getNickname() + " 님이 " + post.getTitle() + " 게시글 참가를 요청했습니다", "참가 요청 알림", postId);

            }
            case "참가 거절" -> {
                log.info("참가 거절 알림");
                User receiver = userRepository.findById(requestDto.getReceiverId()).orElseThrow();
                if (!Objects.equals(post.getUser().getId(), loginUser.getId())) {
                    throw new IllegalArgumentException("게시글 작성자가 아닙니다. 알림을 보낼 수 없습니다.");
                }
                send(loginUser, receiver, requestDto.getType(),
                        loginUser.getNickname() + " 님이 " + post.getTitle() + " 게시글 참가를 거절했습니다", "요청 거절 알림", postId);
            }
        }

    }

    public SseEmitter subscribe(Long userId, String lastEventId) {
        log.info("SSE subscribe: USER ID: " + userId + "LastEVENTID: " + lastEventId);
        User user = userRepository.findById(userId).orElseThrow();

        String emitterId = makeTimeIncludeId(userId);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 503 에러를 방지하기 위한 더미 이벤트 전송

        Notification notification = Notification.builder()
                .title("connect")
                .content("EventStream Created. [userId=" + userId + "]")
                .receiver(user)
                .notificationType(NotificationType.MAKE_CONNECTION)
                .build();
        String eventId = makeNotificationIdIncludeId(userId, notification.getId());
        sendNotification(emitter, eventId, emitterId, notification);

        // 클라이언트가 미수신한 Event 목록이 존재할 경우 전송하여 Event 유실을 예방
        // TODO TEST 정상 동작하는지 확인 필요
        if (hasLostData(lastEventId)) {
            log.info("미수신 데이터 있음");
            sendLostData(lastEventId, userId, emitterId, emitter);
        }
        return emitter;
    }

    public void send(User publisher, User receiver, NotificationType notificationType, String content, String title,
                     Long postId) {
        log.info("send 실행");
        Notification notification = notificationRepository.save(
                createNotification(publisher, receiver, notificationType, content, title, postId));

        String receiverId = String.valueOf(receiver.getId());
        String eventId = makeNotificationIdIncludeId(receiver.getId(), notification.getId());
        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartWithById(receiverId);
        log.info("emitters 크기: " + emitters.size());
        if (emitters.size() > 0) {
            emitters.forEach(
                    (key, emitter) -> {
                        emitterRepository.saveEventCache(eventId, notification);
                        log.info("eventCache 저장 key: " + eventId + " notification: " + notification);
                        sendNotification(emitter, eventId, key, notification);
                    }
            );
        } else {
            log.info("연결된 Emitter가 없는 경우");
            log.info("eventCache 저장 key: " + eventId + " notification: " + notification);
            emitterRepository.saveEventCache(eventId, notification);
        }
    }

    private Notification createNotification(User publisher, User receiver, NotificationType notificationType,
                                            String content, String title, Long postId) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .publisher(publisher)
                .notificationType(notificationType)
                .content(content)
                .title(title)
                .postId(postId)
                .isRead(!Objects.equals(notificationType, NotificationType.JOIN_REQUEST))
                .build();

        return notification;
    }

    private boolean hasLostData(String lastEventId) {
        return !(lastEventId.endsWith("null") || lastEventId.isEmpty());
    }

    private void sendLostData(String lastEventId, Long userId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository.findAllEventCacheStartWithById(
                String.valueOf(userId));
        log.info("eventCaches.size(): " + eventCaches.size());
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> {
                    log.info("entry.getKey(): " + entry.getKey() + "entry.getValue(): " + entry.getValue());
                    sendNotification(emitter, entry.getKey(), emitterId, entry.getValue());
                });
        log.info("미수신 데이터 모두 전송 완료");
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            log.info("sendNotification 실행");
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
        }
    }

    private String makeTimeIncludeId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    private String makeNotificationIdIncludeId(Long userId, Long notificationId) {
        return userId + "_" + notificationId;
    }

    @Transactional
    public void updateRead(long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("알림이 존재하지 않습니다."));
        notification.updateIsRead();
        emitterRepository.deleteAllEventCacheEndsWithNotificationId(id);
        log.info("읽었음으로 상태 변경" + notification.getIsRead().toString());
        log.info("cache 에서 notificationId 로끝나는 키를 가진 notification 삭제");
    }

    //읽음 처리 되지 않은 특정 게시글에 대한 내 알림 목록
    public List<NotificationResponseDto> getNotificationByUserId(Long userId) {
        return notificationRepository.findNotificationByReceiver_IdAndIsReadIsFalse(userId)
                .stream()
                .map(NotificationResponseDto::new)
                .toList();
    }
}
