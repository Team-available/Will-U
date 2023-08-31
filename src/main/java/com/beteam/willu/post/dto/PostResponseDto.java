package com.beteam.willu.post.dto;

import com.beteam.willu.common.ApiResponseDto;
import com.beteam.willu.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostResponseDto extends ApiResponseDto {

    private Long id;
    private String title;
    private String content;
    //TODO
    private String username; // 뭘 넣어야 하나?
    private LocalDateTime promiseTime;
    private String promiseArea;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Long maxnum;
    private Long score;
    private String category;

    public PostResponseDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.username = post.getUser().getUsername();
        this.content = post.getContent();
        this.promiseTime = post.getPromiseTime();
        this.promiseArea = post.getPromiseArea();
        this.createdAt = post.getCreatedAt();
        this.modifiedAt = post.getModifiedAt();
        this.maxnum = post.getMaxnum();
        this.score = post.getUser().getScore();
        this.category = post.getCategory();
    }
}
