package io.digiservices.authorizationserver.service.impl;

import io.digiservices.authorizationserver.model.User;
import io.digiservices.authorizationserver.repository.UserRepository;
import io.digiservices.authorizationserver.service.UserService;
import io.digiservices.authorizationserver.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl  implements UserService {
    private final UserRepository userRepository;
    @Override
    public User getUserByEmail(String email) {
        return userRepository.getUserByEmail(email);
    }

    @Override
    public void resetLoginAttempts(String userUuid) {
        userRepository.resetLoginAttempts(userUuid);
    }

    @Override
    public void updateLoginAttempts(String email) {

        userRepository.updateLoginAttempts(email);
    }

    @Override
    public void setLastLogin(Long userId) {
        userRepository.setLastLogin(userId);
    }

    @Override
    public void addLoginDevice(Long userId, String deviceName, String client, String ipAddress) {

         userRepository.addLoginDevice(userId,deviceName,client,ipAddress);
    }

    @Override
    public boolean verifyQrCode(String userUuid, String code) {
        User user = userRepository.getUserByUuid(userUuid);
        return UserUtils.verifyQrCode(user.getQrCodeSecret(),code);
    }
}
