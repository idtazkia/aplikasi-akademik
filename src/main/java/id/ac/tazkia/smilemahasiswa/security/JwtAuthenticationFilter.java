package id.ac.tazkia.smilemahasiswa.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import id.ac.tazkia.smilemahasiswa.dao.UserDao;
import id.ac.tazkia.smilemahasiswa.entity.Permission;
import id.ac.tazkia.smilemahasiswa.entity.User;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDao userDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                // Validate token
                if (jwtUtil.validateToken(jwt)) {
                    String username = jwtUtil.getUsernameFromToken(jwt);

                    // Load user from database
                    User user = userDao.findByUsername(username);

                    if (user != null) {
                        // Get authorities from user's role
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

                        if (user.getRole() != null && user.getRole().getPermissions() != null) {
                            authorities = user.getRole().getPermissions().stream()
                                    .map(Permission::getValue)
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList());
                        }

                        // Create authentication token
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
                                null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in security context
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                        LOGGER.debug("JWT authentication successful for user: {}", username);
                    } else {
                        LOGGER.warn("User not found for username: {}", username);
                    }
                } else {
                    // Token validation failed (expired, invalid, etc)
                    LOGGER.warn("Invalid or expired JWT token for request: {}", request.getRequestURI());
                    
                    // Only send error response for API endpoints
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write("{\"error\":\"Token expired or invalid. Please request a new token.\",\"status\":401}");
                        return; // Don't continue filter chain
                    }
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }
}
