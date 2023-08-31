package com.beteam.willu.post.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostRequestDto {

    private String title;
    private String content;
    private LocalDateTime promiseTime;
    private String promiseArea;
    private Long maxnum;
    private String category;
}
