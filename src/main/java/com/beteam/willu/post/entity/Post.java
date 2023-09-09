package com.beteam.willu.post.entity;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.beteam.willu.common.Timestamped;
import com.beteam.willu.post.dto.PostRequestDto;
import com.beteam.willu.stomp.entity.ChatRoom;
import com.beteam.willu.user.entity.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post extends Timestamped {

	/**
	 * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "post_name")
	private String title;

	@Column(name = "post_content")
	private String content;

	@Builder.Default
	private Boolean recruitment = true;

	@Column(name = "post_time")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDateTime promiseTime;

	@Column(name = "post_area")
	private String promiseArea;

	@Column(name = "post_maxnum")
	private Long maxnum; //모집 최대 인원

	@Column(name = "post_category")
	private String category;

	/**
	 * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
	 */
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@OneToOne(mappedBy = "post", cascade = CascadeType.REMOVE)
	private ChatRoom chatRoom;

	/**
	 * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
	 */
	public Post(PostRequestDto postRequestDto) {
		if (postRequestDto.getTitle() != null) {
			this.title = postRequestDto.getTitle();
		}
		if (postRequestDto.getContent() != null) {
			this.content = postRequestDto.getContent();
		}
		if (postRequestDto.getPromiseTime() != null) {
			this.promiseTime = postRequestDto.getPromiseTime();
		}
		if (postRequestDto.getPromiseArea() != null) {
			this.promiseArea = postRequestDto.getPromiseArea();
		}
		if (postRequestDto.getMaxnum() != null) {
			this.maxnum = postRequestDto.getMaxnum();
		}
		if (postRequestDto.getCategory() != null) {
			this.category = postRequestDto.getCategory();
		}
	}

	/**
	 * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
	 */
	/**
	 * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
	 */

	public void updateRecruitment(boolean bool) {
		this.recruitment = bool;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setRecruitment(Boolean recruitment) {
		this.recruitment = recruitment;
	}

	public void update(PostRequestDto postRequestDto) {
		if (postRequestDto.getTitle() != null) {
			this.title = postRequestDto.getTitle();
		}
		if (postRequestDto.getContent() != null) {
			this.content = postRequestDto.getContent();
		}
		if (postRequestDto.getPromiseTime() != null) {
			this.promiseTime = postRequestDto.getPromiseTime();
		}
		if (postRequestDto.getPromiseArea() != null) {
			this.promiseArea = postRequestDto.getPromiseArea();
		}
		if (postRequestDto.getMaxnum() != null) {
			this.maxnum = postRequestDto.getMaxnum();
		}
		if (postRequestDto.getCategory() != null) {
			this.category = postRequestDto.getCategory();
		}
	}

}
