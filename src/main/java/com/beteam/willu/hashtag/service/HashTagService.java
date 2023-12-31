package com.beteam.willu.hashtag.service;

import static com.beteam.willu.hashtag.entity.QBoardTagMap.*;
import static com.beteam.willu.hashtag.entity.QTag.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.beteam.willu.hashtag.dto.TagTopResponseDto;
import com.beteam.willu.hashtag.entity.BoardTagMap;
import com.beteam.willu.hashtag.entity.Tag;
import com.beteam.willu.hashtag.repository.BoardTagMapRepository;
import com.beteam.willu.hashtag.repository.HashTagRepository;
import com.beteam.willu.post.dto.PostResponseDto;
import com.beteam.willu.post.entity.Post;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HashTagService {
	private final HashTagRepository hashTagRepository;
	private final BoardTagMapRepository boardTagMapRepository;
	private final JPAQueryFactory queryFactory;

    //태그 검색
    @Transactional
    public Page<PostResponseDto> createTag(String requestDto, boolean recruitmentTag, Pageable pageable) {

		// 태그 테이블을 이용해 사용자가 검색한 내용의 태그의 id를 가져와야함
		Tag tags = hashTagRepository.findByContent(requestDto);

		// 가져온 태그의 id로 게시글들 조회
		List<BoardTagMap> boardTagMapList = boardTagMapRepository.findAllByTagId(tags.getId());

        List<Post> postList = new ArrayList<>();
        for (BoardTagMap boardTagMap : boardTagMapList) {
            if (!recruitmentTag) {
                if (boardTagMap.getPost().getRecruitment()) {
                    postList.add(boardTagMap.getPost());
                }
            } else {
                postList.add(boardTagMap.getPost());
            }
        }

		// 시간순 정렬
		Collections.sort(postList, new PostListComparator().reversed());

		Page<Post> postPage = new PageImpl<>(postList, pageable, postList.size()); // PageImpl 생성자를 사용하여 변환

		return postPage.map(PostResponseDto::new);
	}

	// 태그중 탐 5
	@Transactional
	public List<TagTopResponseDto> getTags() {

		return queryFactory
			.select(Projections.constructor(
				TagTopResponseDto.class,
				tag.content,
				boardTagMap.count()
			))
			.from(boardTagMap)
			.join(boardTagMap.tag, tag)
			.groupBy(tag.content)
			.orderBy(boardTagMap.count().desc())
			.limit(10) // 상위 10개만 제한
			.fetch();
	}

}

class PostListComparator implements Comparator<Post> {
	@Override
	public int compare(Post f1, Post f2) {
		if (f1.getCreatedAt().isAfter(f2.getCreatedAt())) {
			return 1;
		} else if (f1.getCreatedAt().isBefore(f2.getCreatedAt())) {
			return -1;
		}
		return 0;
	}

}
