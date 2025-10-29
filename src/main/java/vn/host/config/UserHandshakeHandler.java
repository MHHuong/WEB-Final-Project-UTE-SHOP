package vn.host.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

/**
 * Handshake handler that sets the Principal name from a query parameter "userId" if present.
 * This is handy for testing per-user messaging without full auth wiring.
 */
public class UserHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(@NonNull ServerHttpRequest request, @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        String name = null;
        if (request instanceof ServletServerHttpRequest servlet) {
            var params = servlet.getServletRequest().getParameterMap();
            if (params.containsKey("userId")) {
                String[] vals = params.get("userId");
                if (vals != null && vals.length > 0 && vals[0] != null && !vals[0].isBlank()) {
                    name = vals[0].trim();
                }
            }
        }
        if (name == null) {
            name = UUID.randomUUID().toString();
        }
        final String principalName = name;
        return new Principal() {
            @Override
            public String getName() {
                return principalName;
            }
        };
    }
}

