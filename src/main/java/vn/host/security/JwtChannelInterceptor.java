package vn.host.security;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import vn.host.entity.User;

import java.security.Principal;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private static final Logger log = LoggerFactory.getLogger(JwtChannelInterceptor.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null) {
                authHeader = accessor.getFirstNativeHeader("authorization");
            }
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String subject = jwtService.getSubject(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(subject);

                    // Extract userId from UserDetails if it's our User entity
                    String principalName = subject; // default to email
                    if (userDetails instanceof User user) {
                        principalName = String.valueOf(user.getUserId());
                        log.debug("WebSocket CONNECT authenticated user={} (userId={})", subject, principalName);
                    } else {
                        log.debug("WebSocket CONNECT authenticated user={} (no userId extraction)", subject);
                    }

                    // Create Authentication with userId as the principal name for user-specific routing
                    final String finalPrincipalName = principalName;
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                        new Principal() {
                            @Override
                            public String getName() {
                                return finalPrincipalName;
                            }
                            @Override
                            public String toString() {
                                return finalPrincipalName;
                            }
                        },
                        null,
                        userDetails.getAuthorities()
                    );
                    accessor.setUser(authentication);
                } catch (Exception ex) {
                    log.warn("Failed to authenticate WebSocket CONNECT with JWT (session={}). Exception: {}", accessor.getSessionId(), ex.toString());
                    log.debug("Full stack:", ex);
                }
            }
        }
        return message;
    }

    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
            String sessionId = accessor != null ? accessor.getSessionId() : "-";
            String dest = accessor != null ? accessor.getDestination() : "-";
            log.error("Exception while processing inbound STOMP message (session={}, dest={})", sessionId, dest, ex);
        }
    }
}

