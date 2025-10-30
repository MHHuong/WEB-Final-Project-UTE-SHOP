package vn.host.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService uds;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
        String token = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            token = auth.substring(7);
        } else {
            // thử đọc cookie nếu header không có
            if (req.getCookies() != null) {
                for (Cookie c : req.getCookies()) {
                    if ("AUTH_TOKEN".equals(c.getName())) {
                        token = c.getValue();
                        break;
                    }
                }
            }
            // optional: cũng có thể đọc query param ?token=...
            if (token == null) {
                String param = req.getParameter("token");
                if (param != null && !param.isBlank()) token = param;
            }
        }

        if (token != null) {
            try {
                String email = jwt.getSubject(token);
                UserDetails user = uds.loadUserByUsername(email);
                var authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception ignored) {
            }
        }
        chain.doFilter(req, res);
    }

}