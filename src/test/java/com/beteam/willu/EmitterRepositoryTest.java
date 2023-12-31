package com.beteam.willu;

import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.beteam.willu.notification.entity.Notification;
import com.beteam.willu.notification.entity.NotificationType;
import com.beteam.willu.notification.repository.EmitterRepository;
import com.beteam.willu.notification.repository.EmitterRepositoryImpl;
import com.beteam.willu.user.entity.User;
import com.beteam.willu.user.repository.UserRepository;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
@DisplayName(value = "EmitterRepositoryImplTest")
class EmitterRepositoryTest {

	private EmitterRepository emitterRepository = new EmitterRepositoryImpl();
	@Autowired
	private UserRepository userRepository;
	private Long DEFAULT_TIMEOUT = 60L * 1000L * 10L;

	@Test
	@Disabled
	@DisplayName("새로운 Emitter를 추가한다.")
	public void save() throws Exception {
		//given
		Long userId = 1L;
		String emitterId = userId + "_" + System.currentTimeMillis();
		SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

		//when, then
		Assertions.assertDoesNotThrow(() -> emitterRepository.save(emitterId, sseEmitter));
	}

	@Test
	@Disabled
	@DisplayName("수신한 이벤트를 캐시에 저장한다.")
	public void saveEventCache() throws Exception {
		//given
		Long userId = 1L;
		String eventCacheId = userId + "_" + System.currentTimeMillis();
		User user = userRepository.findById(userId).orElseThrow();
		Notification notification = Notification.builder()
			.receiver(user)
			.title("http://43.201.154.114:8080/")
			.notificationType(NotificationType.JOIN_REQUEST)
			.content("채팅 참가 신청이 왔습니다.")
			.isRead(false)
			.build();

		//when, then
		Assertions.assertDoesNotThrow(() -> emitterRepository.saveEventCache(eventCacheId, notification));
	}

	@Test
	@Disabled
	@DisplayName("어떤 회원이 접속한 모든 Emitter를 찾는다")
	public void findAllEmitterStartWithByUserId() throws Exception {
		//given
		Long userId = 1L;
		String emitterId1 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

		Thread.sleep(100);
		String emitterId2 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

		Thread.sleep(100);
		String emitterId3 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));

		//when
		Map<String, SseEmitter> ActualResult = emitterRepository.findAllEmitterStartWithById(
			String.valueOf(userId));

		//then
		Assertions.assertEquals(3, ActualResult.size());
	}

	@Test
	@Disabled
	@DisplayName("어떤 회원에게 수신된 이벤트를 캐시에서 모두 찾는다.")
	public void findAllEventCacheStartWithByUserId() throws Exception {
		//given
		Long userId = 1L;
		String eventCacheId1 = userId + "_" + System.currentTimeMillis();
		User user1 = userRepository.findById(1L).orElseThrow();
		Notification notification1 = Notification.builder()
			.receiver(user1)
			.publisher(user1)
			.notificationType(NotificationType.JOIN_REQUEST)
			.content("hi1")
			.title("title1")
			.isRead(false)
			.build();
		emitterRepository.saveEventCache(eventCacheId1, notification1);

		Thread.sleep(100);
		String eventCacheId2 = userId + "_" + System.currentTimeMillis();
		//User user2 = userRepository.findById(2L).orElseThrow();
		Notification notification2 = Notification.builder()
			.receiver(user1)
			.publisher(user1)
			.notificationType(NotificationType.JOIN_REQUEST)
			.content("hi2")
			.title("title2")
			.isRead(false)
			.build();
		emitterRepository.saveEventCache(eventCacheId2, notification2);

		Thread.sleep(100);
		String eventCacheId3 = userId + "_" + System.currentTimeMillis();
		//User user3 = userRepository.findById(3L).orElseThrow();
		Notification notification3 = Notification.builder()
			.receiver(user1)
			.publisher(user1)
			.notificationType(NotificationType.JOIN_REQUEST)
			.content("hi3")
			.title("title3")
			.isRead(false)
			.build();
		emitterRepository.saveEventCache(eventCacheId3, notification3);

		//when
		Map<String, Object> ActualResult = emitterRepository.findAllEventCacheStartWithById(
			String.valueOf(userId));

		for (String key : ActualResult.keySet()) {
			Notification nt = (Notification)ActualResult.get(key);
			System.out.println(nt.getTitle());
		}
		//then

		Assertions.assertEquals(3, ActualResult.size());
	}
/*
	@Test
	@DisplayName("ID를 통해 Emitter를 Repository에서 제거한다.")
	public void deleteById() throws Exception {
		//given
		Long userId = 1L;
		String emitterId = userId + "_" + System.currentTimeMillis();
		SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

		//when
		emitterRepository.save(emitterId, sseEmitter);
		emitterRepository.deleteById(emitterId);

		//then
		Assertions.assertEquals(0, emitterRepository.findAllEmitterStartWithByUserId(emitterId).size());
	}

	@Test
	@DisplayName("저장된 모든 Emitter를 제거한다.")
	public void deleteAllEmitterStartWithId() throws Exception {
		//given
		Long userId = 1L;
		String emitterId1 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

		Thread.sleep(100);
		String emitterId2 = userId + "_" + System.currentTimeMillis();
		emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

		//when
		emitterRepository.deleteAllEmitterStartWithId(String.valueOf(userId));

		//then
		Assertions.assertEquals(0,
			emitterRepository.findAllEmitterStartWithByUserId(String.valueOf(userId)).size());
	}

	@Test
	@DisplayName("수신한 이벤트를 캐시에 저장한다.")
	public void deleteAllEventCacheStartWithId() throws Exception {
		//given
		Long userId = 1L;
		String eventCacheId1 = userId + "_" + System.currentTimeMillis();
		Notification notification1 = new Notification(new User(1L), NotificationType.APPLY, "스터디 신청이 왔습니다.", "url",
			false);
		emitterRepository.saveEventCache(eventCacheId1, notification1);

		Thread.sleep(100);
		String eventCacheId2 = userId + "_" + System.currentTimeMillis();
		Notification notification2 = new Notification(new User(1L), NotificationType.ACCEPT, "스터디 신청이 승인되었습니다.",
			"url", false);
		emitterRepository.saveEventCache(eventCacheId2, notification2);

		//when
		emitterRepository.deleteAllEventCacheStartWithId(String.valueOf(userId));

		//then
		Assertions.assertEquals(0,
			emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId)).size());
	}*/
}