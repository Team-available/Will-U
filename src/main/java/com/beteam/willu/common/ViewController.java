package com.beteam.willu.common;

import com.beteam.willu.post.dto.PostResponseDto;
import com.beteam.willu.post.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
@Slf4j
public class ViewController {

    @Autowired
    private PostService postService;

    //메인화면
    @GetMapping("/index")    //주소 입력값
    public String postsView(Model model) {
        List<PostResponseDto> posts = postService.getPosts();
        model.addAttribute("posts", posts);
        return "index"; //출력 html
    }

    //게시글 작성
    @GetMapping("/post/create")
    public String createPost() {
        return "createPost";
    }

    //게시글 단건 조회
    @GetMapping("/posts/{postId}")
    public String detailPost(Model model, @PathVariable Long postId) {
        PostResponseDto post = postService.getPost(postId);
        model.addAttribute("post", post);
        return "detailPost";
    }

    // 보드 수정 페이지
    @GetMapping("/posts/update/{postId}")
    public String updatePost(Model model, @PathVariable Long postId) {
        model.addAttribute("postId", postId);//2 값을 1에 담음 타임리프 가져올꺼면 이름 "postId" 로 가져오기
        return "updatePost";
    }

}
