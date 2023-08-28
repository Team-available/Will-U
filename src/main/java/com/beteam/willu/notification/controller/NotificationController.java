package com.beteam.willu.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.beteam.willu.common.ApiResponseDto;
import com.beteam.willu.common.security.UserDetailsImpl;
import com.beteam.willu.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationController {
	private final NotificationService notificationService;

	//현재 로그인한 ID에 대한(본인) 구독으로 EventStream 생성 하도록 함. 후에 수정 여지 있음
	@GetMapping(value = "/subscribe", produces = "text/event-stream")
	@ResponseBody
	public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
		return notificationService.subscribe(userDetails.getUser().getId(), lastEventId);
	}

	//알림 읽음 상태 수정 안읽음 <-> 읽음
	@PatchMapping("/api/notification/{id}")
	public ResponseEntity<ApiResponseDto> updateNotification(@PathVariable long id) {
		notificationService.updateRead(id);
		return ResponseEntity.ok().body(new ApiResponseDto("읽음 상태 처리 완료", 200));
	}

	//알림 테스트 화면
	@GetMapping("/notification-page")
	public String notificationPage() {
		return "Notification";
	}
}