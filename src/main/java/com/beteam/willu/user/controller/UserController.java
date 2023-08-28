package com.beteam.willu.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.beteam.willu.common.ApiResponseDto;
import com.beteam.willu.common.security.UserDetailsImpl;
import com.beteam.willu.user.dto.UserRequestDto;
import com.beteam.willu.user.dto.UserResponseDto;
import com.beteam.willu.user.dto.UserUpdateRequestDto;
import com.beteam.willu.user.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {
	private final UserService userService;

	@PostMapping("/users/signup")
	public ResponseEntity<ApiResponseDto> userSignup(@RequestBody UserRequestDto requestDto) {
		userService.userSignup(requestDto);
		return ResponseEntity.status(201).body(new ApiResponseDto("회원가입 성공", 201));
	}

	@PostMapping("/users/login")
	public ResponseEntity<ApiResponseDto> login(@RequestBody UserRequestDto requestDto, HttpServletResponse response) {
		userService.userLogin(requestDto, response);
		return ResponseEntity.ok().body(new ApiResponseDto("로그인 성공", 200));
	}

	//로그아웃 확인용 API  redis 에 토큰을 추가하는 행위임으로 POST 사용
	@PostMapping("/users/logout")
	public ResponseEntity<ApiResponseDto> logout(@CookieValue(name = "Authorization") String accessToken,
		HttpServletResponse response, @AuthenticationPrincipal UserDetailsImpl userDetails) {
		userService.logout(accessToken, response, userDetails.getUsername());

		return ResponseEntity.ok().body(new ApiResponseDto("로그아웃 성공", 200));
	}

	// 유저 조회 (프로파일)
	@GetMapping("/users/{id}")
	public ResponseEntity<UserResponseDto> getProfile(@PathVariable Long id) {
		return ResponseEntity.ok(userService.getProfile(id));
	}

	// 유저 업데이트(프로파일)
	@PutMapping("/users")
	public ResponseEntity<UserResponseDto> userUpdate(@RequestBody UserUpdateRequestDto updateRequestDto,
		@AuthenticationPrincipal UserDetailsImpl user) {
		return ResponseEntity.ok(userService.userUpdate(updateRequestDto, user));
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<ApiResponseDto> deleteUser(@PathVariable Long id,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		userService.deleteUser(id, userDetails.getUser());
		return ResponseEntity.ok(new ApiResponseDto("회원 탈퇴 성공", 200));
	}

}