package vn.host.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import vn.host.dto.auth.AuthRes;
import vn.host.service.AuthService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        log.info("OAuth2 login successful, user email: {}", email);
        AuthRes authRes = authService.processOAuth2User(email, name);

        String redirectUrl = UriComponentsBuilder.fromUriString("/")
                .queryParam("token", authRes.token())
                .queryParam("email", authRes.email()) // Gửi kèm thêm thông tin nếu frontend cần
                .queryParam("name", authRes.fullName())
                .queryParam("role", authRes.role())
                .queryParam("userId", authRes.userId())
                .build().toUriString();

        log.debug("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}