package com.ngo.ngoapp.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.ngo.ngoapp.auth.UserPrincipal;
import com.ngo.ngoapp.models.User;
import com.ngo.ngoapp.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            if (token.startsWith("\"") && token.endsWith("\"") && token.length() > 2) {
                token = token.substring(1, token.length() - 1).trim();
            }
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }
            if (!token.isEmpty()) {
                try {
                    DecodedJWT jwt = JWT.decode(token);
                    String sub = jwt.getSubject();
                    String email = jwt.getClaim("email").asString();
                    String name = jwt.getClaim("name").asString();
                    String role = jwt.getClaim("role").asString();
                    if (role == null) {
                        String[] roles = jwt.getClaim("https://ngo-app.com/roles").asArray(String.class);
                        if (roles != null && roles.length > 0) {
                            role = roles[0];
                        }
                    }

                    if (sub != null || email != null) {
                        User user = resolveUser(sub, email, name, role);
                        UserPrincipal principal = new UserPrincipal(user);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        principal, null, principal.getAuthorities()
                                );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse JWT token from request: {}", e.getMessage());
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private User resolveUser(String sub, String email, String name, String roleFromToken) {
        String effectiveRole = (roleFromToken != null && !roleFromToken.trim().isEmpty()) ? roleFromToken.toUpperCase() : "USER";

        if (email != null && !email.isEmpty()) {
            Optional<User> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent()) {
                User user = existingByEmail.get();
                if (roleFromToken != null && !roleFromToken.trim().isEmpty() && !user.getRole().equalsIgnoreCase(roleFromToken)) {
                    user.setRole(effectiveRole);
                    return userRepository.save(user);
                }
                return user;
            }
        }

        if (sub != null && !sub.isEmpty()) {
            Optional<User> existingById = userRepository.findById(sub);
            if (existingById.isPresent()) {
                User user = existingById.get();
                if (roleFromToken != null && !roleFromToken.trim().isEmpty() && !user.getRole().equalsIgnoreCase(roleFromToken)) {
                    user.setRole(effectiveRole);
                    return userRepository.save(user);
                }
                return user;
            }
        }

        User newUser = new User();
        newUser.setId(sub != null ? sub : java.util.UUID.randomUUID().toString());
        newUser.setEmail(email != null ? email : (sub != null ? sub : "user@example.com"));
        newUser.setName(name != null ? name : "User");
        newUser.setRole(effectiveRole);
        newUser.setCreatedAt(LocalDateTime.now());
        return userRepository.save(newUser);
    }
}
