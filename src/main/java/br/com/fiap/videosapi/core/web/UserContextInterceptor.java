package br.com.fiap.videosapi.core.web;

import br.com.fiap.videosapi.core.context.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Component
public class UserContextInterceptor implements HandlerInterceptor {

    private static final String USER_HEADER = "x-cliente-id";

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String userId = request.getHeader(USER_HEADER);

        // Allow actuator and swagger without user header
        String uri = request.getRequestURI();
        boolean isPublic = uri.contains("/actuator") || uri.contains("/swagger") || uri.contains("/v3/api-docs");

        if (userId == null || userId.isBlank()) {
            if (isPublic) {
                return true;
            }
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Missing required header x-cliente-id\"}");
            return false;
        }

        UserContext.setUserId(userId);
        response.setHeader(USER_HEADER, userId);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, @Nullable Exception ex) {
        UserContext.clear();
    }
}
