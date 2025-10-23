package vn.host.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // DEV only: tắt CSRF để form/POST không bị chặn
                .csrf(AbstractHttpConfigurer::disable)

                // Cho phép TẤT CẢ endpoint (bao gồm static resources)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/**",
                                "/error", "/error/**",
                                "/favicon.ico",
                                // static
                                "/css/**", "/js/**", "/images/**", "/webjars/**", "/lib/**",
                                // nếu có các module trang riêng
                                "/dashboard/**", "/docs/**", "/pages/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )

                // Tắt cơ chế đăng nhập mặc định
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // Cho phép gọi logout mà không cần auth
                .logout(logout -> logout.permitAll())

                // Nếu có dùng H2-console hoặc cần nhúng frame, có thể mở frameOptions
                .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}
