package io.digiservices.userservice.repository.impl;


import io.digiservices.userservice.exception.ApiException;
import io.digiservices.userservice.model.*;
import io.digiservices.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static io.digiservices.userservice.query.UserQuery.*;
import static io.digiservices.userservice.utils.UserUtils.*;
import static java.sql.Types.VARCHAR;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRepositoryImpl implements UserRepository {

    private final JdbcClient jdbcClient;

    @Override
    public User getUserByEmail(String email) {
        try {
            return jdbcClient.sql(SELECT_USER_BY_EMAIL_QUERY).param("email",email).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("No user found by email");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User getUserByUuid(String userUuid) {
        try {
            return jdbcClient.sql(SELECT_USER_BY_UUID_QUERY).param("userUuid",userUuid).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("No user found by userUuid");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User getUserById(Long userId) {
        try {
            return jdbcClient.sql(SELECT_USER_BY_ID_QUERY).param("userId",userId).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("No user found by userId");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User updateUser(String userUuid, String firstName, String lastName, String email, String phone, String bio, String address) {
        try {
            return jdbcClient.sql(UPDATE_USER_FUNCTION).paramSource(getParamSource( userUuid,  firstName,  lastName,  email,  phone,  bio,  address)).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("No user found by userUuid");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public String createUser(String firstName, String lastName, String email, String username, String password) {
        try {
            var token = UUID.randomUUID().toString();
            jdbcClient.sql(CREATE_USER_STORED_PROCEDURE).paramSource(getParamSource( firstName,  lastName,  email,  username,  password,  token)).query(User.class).single();
            return  token;
        }catch (DuplicateKeyException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Email is already in Use. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public AccountToken getAccountToken(String token) {
        try {
            return jdbcClient.sql(SELECT_ACCOUNT_TOKEN_QUERY).param("token",token).query(AccountToken.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Invalid Link. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User verifyPasswordToken(String token) {
        return null;
    }

    @Override
    public User enableMfa(String userUuid) {
        try {
            return jdbcClient.sql(ENABLE_USER_MFA_FUNCTION).paramSource(getParamSource(userUuid,qrCodeSecret.get())).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not found, Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User disableMfa(String userUuid) {
        try {
            return jdbcClient.sql(DISABLE_USER_MFA_FUNCTION).param("userUuid",userUuid).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not found, Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }



    @Override
    public User toggleAccountExpired(String userUuid) {
        try {
            return jdbcClient.sql(TOGGLE_ACCOUNT_EXPIRED_FUNCTION).param("userUuid",userUuid).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not found, Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User toggleAccountLocked(String userUuid) {
        try {
            return jdbcClient.sql(TOGGLE_ACCOUNT_LOCKED_FUNCTION).param("userUuid",userUuid).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not found, Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User toggleAccountEnabled(String userUuid) {
        try {
            return jdbcClient.sql(TOGGLE_ACCOUNT_ENABLED_FUNCTION).param("userUuid",userUuid).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not found, Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User toggleCredentialsExpired(String userUuid) {
        return null;
    }

    @Override
    public void updatePassword(String userUuid, String encodedPassword) {
        try {
            jdbcClient.sql(UPDATE_USER_PASSWORD_QUERY).params(Map.of("userUuid",userUuid,"password",encodedPassword)).update();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not found, Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }

    }



    @Override
    public User updateRole(String userUuid, String role) {
        try {
            return jdbcClient.sql(UPDATE_USER_ROLE_FUNCTION).params(Map.of("userUuid",userUuid,"role",role)).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not found, Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public void resetPassword(String email) {

    }

    @Override
    public void doResetPassword(String userUuid, String token, String password, String confirmPassword) {

    }

    @Override
    public List<User> getUsers() {
        try {
           return jdbcClient.sql(SELECT_USER_QUERY).query(User.class).list();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Users Not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public User getAssignee(String ticketUuid) {
        try {
            return jdbcClient.sql(SELECT_TICKET_ASSIGNEE_QUERY).param("ticketUuid", ticketUuid).query(User.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            log.error("Ticket is not assigned.");
            return User.builder().build();
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public Credential getCredential(String userUuid) {
        try {
            return jdbcClient.sql(SELECT_USER_CREDENTIAL_QUERY).param("userUuid",userUuid).query(Credential.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Credential not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public List<Device> getDevices(String userUuid) {
        try {
            return jdbcClient.sql(SELECT_USER_DEVICES_QUERY).param("userUuid",userUuid).query(Device.class).list();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Not Devices Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public void deleteAccountToken(String token) {
        try {
            jdbcClient.sql(DELETE_ACCOUNT_TOKEN_QUERY).param("token",token).update();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Token not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public void updateAccountSettings(Long userId) {
        try {
            jdbcClient.sql(UPDATE_ACCOUNT_SETTINGS_QUERY).param("userId",userId).update();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public PasswordToken getPasswordToken(String token) {
        try {
            return jdbcClient.sql(SELECT_PASSWORD_TOKEN_QUERY).param("token",token).query(PasswordToken.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Invalid Link. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public PasswordToken getPasswordToken(Long userId) {
        try {
            return jdbcClient.sql(SELECT_PASSWORD_TOKEN_BY_USER_ID_QUERY).param("userId",userId).query(PasswordToken.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Invalid Link. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public void deletePasswordToken(Long userId) {
        try {
            jdbcClient.sql(DELETE_PASSWORD_TOKEN_QUERY).param("userId",userId).update();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Token not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public void deletePasswordToken(String token) {
        try {
            jdbcClient.sql("").param("token",token).update();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Token not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public void updateImageUrl(String userUuid, String imageUrl) {
        try {
            jdbcClient.sql(UPDATE_USER_IMAGE_URI_QUERY).params(Map.of("userUuid",userUuid,"imageUrl",imageUrl)).update();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("User not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public String getPassword(String userUuid) {
        try {
            return jdbcClient.sql(SELECT_PASSWORD_USER_QUERY).param("userUuid",userUuid).query(String.class).single();
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Password Not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    @Override
    public String createPasswordToken(Long userId) {
        try {
            var token = randomUUID.get();
            jdbcClient.sql(CREATE_PASSWORD_TOKEN_QUERY).params(Map.of("userId",userId,"token", token)).query(String.class).single();
            return token;
        }catch (EmptyResultDataAccessException exception){
            log.error(exception.getMessage());
            throw  new ApiException("Password Not Found. Please try again");
        }catch (Exception e){
            log.error(e.getMessage());
            throw  new ApiException("An error occurred please try again");
        }
    }

    private SqlParameterSource getParamSource(String userUuid, String qrCodeSecret)
    {
        return new MapSqlParameterSource()
                .addValue("userUuid",userUuid, VARCHAR)
                .addValue("qrCodeSecret",qrCodeSecret, VARCHAR)
                .addValue("qrCodeImageUri",qrCodeImageUri.apply(qrCodeSecret), VARCHAR);
    }

    private SqlParameterSource getParamSource(String firstName, String lastName, String email, String username, String password, String token)
    {
        return new MapSqlParameterSource()
                .addValue("userUuid",randomUUID.get(), VARCHAR)
                .addValue("firstName",firstName, VARCHAR)
                .addValue("lastName",lastName, VARCHAR)
                .addValue("email",email.trim().toLowerCase(), VARCHAR)
                .addValue("username",username.trim().toLowerCase(), VARCHAR)
                .addValue("memberId", memberId.get(), VARCHAR)
                .addValue("credentialUuid", randomUUID.get(), VARCHAR)
                .addValue("password",password, VARCHAR)
                .addValue("token",token, VARCHAR);
    }

    private SqlParameterSource getParamSource(String userUuid, String firstName, String lastName, String email, String phone, String bio, String address)
    {
        return new MapSqlParameterSource()
                .addValue("userUuid",userUuid, VARCHAR)
                .addValue("firstName",firstName, VARCHAR)
                .addValue("lastName",lastName, VARCHAR)
                .addValue("email",email.trim().toLowerCase(), VARCHAR)
                .addValue("phone",phone, VARCHAR)
                .addValue("bio",bio, VARCHAR)
                .addValue("address",address, VARCHAR);
    }


}
