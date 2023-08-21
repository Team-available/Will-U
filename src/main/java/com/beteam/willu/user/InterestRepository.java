package com.beteam.willu.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    boolean existsByReceiverIdAndSenderId(Long receiver, Long sender);

    Optional<Interest> findByReceiverIdAndSenderId(Long receiver, Long sender);
}