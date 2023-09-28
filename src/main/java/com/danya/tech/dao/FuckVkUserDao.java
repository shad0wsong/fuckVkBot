package com.danya.tech.dao;

import com.danya.tech.entity.FuckVkUser;
import com.danya.tech.repositories.FuckVkUserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FuckVkUserDao {

    final FuckVkUserRepository fuckVkUserRepository;

    public void saveFuckVkUser(FuckVkUser fuckVkUser) {
        Optional<FuckVkUser> userOptional = fuckVkUserRepository.findByChatId(fuckVkUser.getChatId());
        userOptional.ifPresentOrElse(user -> setNewAuthTokenAndSave(user, fuckVkUser.getAuthToken()),
                () -> createNewUserAndSave(fuckVkUser));
    }

    private void setNewAuthTokenAndSave (FuckVkUser fuckVkUser, String newAuthToken) {
        fuckVkUser.setAuthToken(newAuthToken);
        fuckVkUserRepository.save(fuckVkUser);
    }

    private void createNewUserAndSave(FuckVkUser fuckVkUser) {
        fuckVkUserRepository.save(fuckVkUser);
    }
}
