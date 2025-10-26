package vn.host.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthenticationSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(reg -> reg
                        .requestMatchers(
                                "/", "/index", "/index.html",
                                "/assets/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/lib/**",
                                "/error", "/error/**",
                                "/favicon.ico",
                                "/dashboard/**",
                                "/docs/**",
                                "/pages/**",
                                "/api/auth/**",
                                "/login",
                                "/register",
                                "/forgot-password"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(out -> out.permitAll())
                .oauth2Login(oauth -> {
                    oauth.loginPage("/login");
                    oauth.authorizationEndpoint(auth -> auth
                            .baseUri("/oauth2/authorization")
                    );
                    oauth.redirectionEndpoint(redir -> redir
                            .baseUri("/login/oauth2/code/*")
                    );
                    oauth.successHandler(oAuth2LoginSuccessHandler);
                });
        return http.build();
    }
}