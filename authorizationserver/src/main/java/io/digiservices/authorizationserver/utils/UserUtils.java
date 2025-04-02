package io.digiservices.authorizationserver.utils;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import io.digiservices.authorizationserver.model.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;

public class UserUtils {

    public static boolean verifyQrCode(String secret,String code){
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator,timeProvider);
        return verifier.isValidCode(secret,code);
    }
    public static User getUser(Authentication authentication){
         if(authentication instanceof OAuth2AuthorizationCodeRequestAuthenticationToken){
             var usernamePasswordAuthentication = (UsernamePasswordAuthenticationToken) authentication.getPrincipal();
             return (User) usernamePasswordAuthentication.getPrincipal();
         }
         return (User) authentication.getPrincipal();
    }
}
