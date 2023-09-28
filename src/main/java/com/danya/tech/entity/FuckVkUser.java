package com.danya.tech.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@ToString
public class FuckVkUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;

    Long chatId;

    String authToken;

    public FuckVkUser(Long chatId, String authToken) {
        this.chatId = chatId;
        this.authToken = authToken;
    }
}
