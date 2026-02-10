package com.springboot.medsystem.Aunthentication;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    private final JwtService jwtService;

    public OAuth2AuthenticationSuccessHandler(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Generate JWT from OAuth user
        assert oAuth2User != null;
        String token = jwtService.generateTokenFromOAuth(oAuth2User);

        // you may persist user here (patient/pharmacy)

        // Redirect to Swagger with token
        response.sendRedirect(
                "/swagger-ui/index.html?token=" + token
        );
    }
}
