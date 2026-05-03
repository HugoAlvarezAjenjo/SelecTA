package es.hugoalvarezajenjo.selecta.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request,
                                        final HttpServletResponse response,
                                        final Authentication authentication) throws IOException, ServletException {
        String targetUrl = "/profile"; // default fallback

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if ("ROLE_ADMIN".equals(role)) {
                targetUrl = "/admin";
                break;
            } else if ("ROLE_TEACHER".equals(role)) {
                targetUrl = "/profile"; // future: "/teacher/dashboard"
                break;
            } else if ("ROLE_STUDENT".equals(role)) {
                targetUrl = "/profile"; // future: "/student/dashboard"
                break;
            }
        }

        response.sendRedirect(targetUrl);
    }
}
