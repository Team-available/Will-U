package com.beteam.willu.interest.entity;

import com.beteam.willu.common.Timestamped;
import com.beteam.willu.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "interests")
public class Interest extends Timestamped {
	/**
	 * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "receiver_id") //받는 사람
	private User receiver;

	@ManyToOne
	@JoinColumn(name = "sender_id") //보내는 사람
	private User sender;

	/**
	 * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
	 */

	public Interest(User receiver, User sender) {
		this.receiver = receiver;
		this.sender = sender;
	}

	/**
	 * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
	 */

	/**
	 * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
	 */

	/**
	 * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
	 */

}
