package io.digiservices.userservice.service;

import io.digiservices.userservice.model.Credential;
import io.digiservices.userservice.model.Device;
import io.digiservices.userservice.model.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    User getUserByEmail(String email);
    User getUserByUuid(String userUuid);
    User updateUser(String userUuid, String firstName, String lastName, String email, String phone, String bio, String address);
    void createUser(String firstName, String lastName, String email, String username, String password);
    void verifyAccount(String token);
    User verifyPasswordToken(String token);
    User enableMfa(String userUuid);
    User disableMfa(String userUuid);
    User uploadPhoto(String userUuid, MultipartFile file);
    User toggleAccountExpired(String userUuid);
    User toggleAccountLocked(String userUuid);
    User toggleAccountEnabled(String userUuid);
    User toggleCredentialsExpired(String userUuid);
    void updatePassword(String userUuid, String currentPassword, String newPassword, String confirmNewPassword);
    User updateRole(String userUuid, String role);
    void resetPassword(String email);
    void doResetPassword(String userUuid, String token, String password, String confirmPassword);
    List<User> getUsers();
    User getAssignee(String ticketUuid);
    Credential getCredential(String userUuid);
    List<Device> getDevices(String userUuid);
}
