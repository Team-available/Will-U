package com.beteam.willu.post;


import com.beteam.willu.common.exception.RecruitmentStatusException;
import com.beteam.willu.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.RejectedExecutionException;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {


    private final PostRepository postRepository;

    // 게시글 작성
    @Override
    public PostResponseDto createPost(PostRequestDto postRequestDto, User user) {
        Post post = new Post(postRequestDto);
        post.setUser(user);
        postRepository.save(post);
        return new PostResponseDto(post);
    }

    // 게시글 전체 조회
    @Override
    public List<PostResponseDto> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> new PostResponseDto(post))
                .toList();
    }
//    @Override
//    public Page<PostResponseDto> getPosts(Pageable pageable) {
//        Page<Post> posts = postRepository.findAll(pageable);
//        return posts.map(post -> new PostResponseDto(post));
//    }

    // 게시글 상세 조회

    public PostResponseDto getPost(Long id) {
        Post post = findPost(id);
        return new PostResponseDto(post);
    }

    // 게시글 수정
    @Transactional
    @Override
    public PostResponseDto updatePost(Long id, PostRequestDto postRequestDto, String username) {
        Post post = findPost(id);
        if (!post.getUser().getUsername().equals(username)) {
            throw new RejectedExecutionException("작성자만 수정 가능합니다.");
        }
        post.update(postRequestDto);
        return new PostResponseDto(post);
    }
    // 게시글 삭제

    @Override
    public void deletePost(Long id, User user) {
        Post post = findPost(id);
        if (!post.getUser().getId().equals(user.getId())) {
            throw new RejectedExecutionException("작성자만 삭제 가능합니다.");
        }
        postRepository.delete(post);
    }

    // 모집완료 -> 모집중으로 변경
    @Override
    @Transactional
    public void activateRecruitment(Long id, User user) {
        Post post = findPost(id);

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RejectedExecutionException("작성자만 변경 가능합니다.");
        }

        if (post.getRecruitment()) {
            throw new RecruitmentStatusException();
        } else {
            post.setRecruitment(true); // 저장 안 됨
            System.out.println(post.getRecruitment());
        }
    }

    // 모집중 -> 모집완료로 변경
    @Override
    @Transactional
    public void completeRecruitment(Long id, User user) {
        Post post = findPost(id);

        if (!post.getUser().getId().equals(user.getId())) {
            throw new RejectedExecutionException("작성자만 변경 가능합니다.");
        }

        if (post.getRecruitment()) {
            post.setRecruitment(false); // 저장 안 됨
        } else {
            throw new RecruitmentStatusException();
        }
    }

    // 게시글 찾기
    @Override
    public Post findPost(Long id) {
        return postRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }
}
