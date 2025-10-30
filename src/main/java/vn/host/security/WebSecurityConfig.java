package vn.host.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthenticationSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                                .requestMatchers(
                                        "/",
                                        "/login",
                                        "/register",
                                        "/forgot-password",
                                        "/shop-grid",
                                        "/shop-grid/**",
                                        "/user/**",
                                        "/products/**",
                                        "/payment/**",
                                        "/ws/**",
                                        "/user/queue/orders/**",
                                        "/css/**",
                                        "/js/**",
                                        "/libs/**",
                                        "/images/**",
                                        "/fonts/**",
                                        "/addresses.json",
                                        "/shipper/register",
                                        "/shop/register"
                                ).permitAll()
                                .requestMatchers(
                                        "/assets/**",
                                        "/webjars/**",
                                        "/uploads/**",
                                        "/error",
                                        "/favicon.ico"
                                ).permitAll()
                                .requestMatchers(
                                        "/api/auth/login",
                                        "/api/auth/register",
                                        "/api/auth/otp/**",
                                        "/api/auth/password/reset"
                                ).permitAll()
                                .requestMatchers("/api/locations/**").permitAll()
                                .requestMatchers("/api/status/**").permitAll()
                                .requestMatchers("/uploads/**", "/shop/account/shop-register").permitAll()
                                .requestMatchers("/api/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/api/products/**", "/api/categories/**").permitAll()
                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/shop/**").hasRole("SELLER")
                                .requestMatchers("/shipper/**").hasRole("SHIPPER")
//                        .requestMatchers("/user/**").authenticated()
                                .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> {
                    oauth.loginPage("/login");
                    oauth.authorizationEndpoint(auth -> auth
                            .baseUri("/oauth2/authorization")
                    );
                    oauth.redirectionEndpoint(redir -> redir
                            .baseUri("/login/oauth2/code/*")
                    );
                    oauth.successHandler(oAuth2LoginSuccessHandler);
                })
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}