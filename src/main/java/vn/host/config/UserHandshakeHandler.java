package vn.host.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class UserHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    @Nullable
    protected Principal determineUser(ServerHttpRequest request,
                                     WebSocketHandler wsHandler,
                                     Map<String, Object> attributes) {
        // Get userId from attributes (set by interceptor)
        String userId = (String) attributes.get("userId");

        if (userId != null) {
            return () -> userId;
        }

        return super.determineUser(request, wsHandler, attributes);
    }
}
