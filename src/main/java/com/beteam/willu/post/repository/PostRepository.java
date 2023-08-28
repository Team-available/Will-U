package com.beteam.willu.post.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.beteam.willu.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
	List<Post> findAllByOrderByCreatedAtDesc();

	// 제목으로 검색
	Page<Post> findByTitleContaining(String keyword, Pageable pageable);

	// 작성자의 username으로 검색
	Page<Post> findByUsernameContaining(String keyword, Pageable pageable);

	// 내용으로 검색
	Page<Post> findByContentContaining(String keyword, Pageable pageable);

	// 제목으로 검색하고 모집 상태가 true인 게시글만 검색
	Page<Post> findByTitleContainingAndRecruitmentIsTrue(String keyword, Pageable pageable);

	// 작성자의 username으로 검색하고 모집 상태가 true인 게시글만 검색
	Page<Post> findByUsernameContainingAndRecruitmentIsTrue(String keyword, Pageable pageable);

	// 내용으로 검색하고 모집 상태가 true인 게시글만 검색
	Page<Post> findByContentContainingAndRecruitmentIsTrue(String keyword, Pageable pageable);

}