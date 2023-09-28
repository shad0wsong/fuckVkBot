package com.danya.tech.repositories;

import com.danya.tech.entity.FuckVkUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FuckVkUserRepository extends JpaRepository <FuckVkUser, Long> {
    Optional<FuckVkUser> findByChatId(Long chatId);
}
