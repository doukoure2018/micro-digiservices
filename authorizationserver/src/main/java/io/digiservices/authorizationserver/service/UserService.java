package io.digiservices.authorizationserver.service;

import io.digiservices.authorizationserver.model.User;
import org.springframework.stereotype.Service;

public interface UserService {
    User getUserByEmail(String email);
    void resetLoginAttempts(String userUuid);
    void updateLoginAttempts(String email);
    void setLastLogin(Long userId);
    void addLoginDevice(Long userId, String deviceName, String client, String ipAddress);
    boolean verifyQrCode(String userUuid, String code);
}
