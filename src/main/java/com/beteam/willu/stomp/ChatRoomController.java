package com.beteam.willu.stomp;

import com.beteam.willu.security.UserDetailsImpl;
import com.beteam.willu.stomp.dto.ChatRoomNameResponseDto;
import com.beteam.willu.stomp.dto.ChatSaveRequestDto;
import com.beteam.willu.stomp.entity.Chat;
import com.beteam.willu.stomp.entity.UserChatRoom;
import com.beteam.willu.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    // 게시물이 생성되었을때 채팅룸 생성
    @PostMapping("/chat/posts/{id}/creatRoom")
    public void createRoom(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
        chatRoomService.createRoom(id,userDetails);
    }

    // 사용자가 속한 채팅방(모두) 불러오기
    @GetMapping("/chat/users/{id}")
    public  List<UserChatRoom> getRooms(@PathVariable Long id){
       return chatRoomService.getRooms(id);
    }

    // 사용자의 id 가져오기
    @GetMapping("/chat/userss/{id}")
    public Long getUser(@PathVariable String id){
     return chatRoomService.getUser(id);
    }

    // 특정 채팅룸 불러오기
    @GetMapping("/chat/posts/{id}")
    public ChatRoomNameResponseDto getRoom(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails){
       return chatRoomService.getRoom(id, userDetails);
    }

    // 생성된 채팅 저장
    @PostMapping("/chat/chatRooms")
    public void createRooms(@RequestBody ChatSaveRequestDto chatSaveRequestDto){
        chatRoomService.createRooms(chatSaveRequestDto);
    }

    // 특정 채팅방 채팅 조회
    @GetMapping("/chat/chatRooms/{id}")
    public List<Chat> getChat(@PathVariable Long id){
      return chatRoomService.getChat(id);

    }

    // 유저 초대 기능


}
