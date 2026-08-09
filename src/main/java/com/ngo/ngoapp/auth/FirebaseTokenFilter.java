package com.ngo.ngoapp.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.ngo.ngoapp.config.FirebaseConfig;
import com.ngo.ngoapp.models.User;
import com.ngo.ngoapp.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class FirebaseTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(FirebaseTokenFilter.class);

    private final FirebaseConfig firebaseConfig;
    private final UserRepository userRepository;

    public FirebaseTokenFilter(FirebaseConfig firebaseConfig, UserRepository userRepository) {
        this.firebaseConfig = firebaseConfig;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String idToken = authHeader.substring(7);
        try {
            String uid;
            String email;
            String name;

            if (firebaseConfig.isInitialized()) {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
                uid = decodedToken.getUid();
                email = decodedToken.getEmail();
                name = (String) decodedToken.getClaims().get("name");
                if (name == null) {
                    name = email != null ? email.split("@")[0] : "User";
                }
            } else {
                log.info("Processing token in Firebase Mock Mode: {}", idToken);
                if ("mock-admin".equals(idToken)) {
                    uid = "mock-admin-uid";
                    email = "admin@ngo.org";
                    name = "Mock Administrator";
                } else if (idToken.startsWith("mock-user")) {
                    uid = idToken;
                    email = idToken + "@example.com";
                    name = "Mock User (" + idToken.replace("mock-user-", "") + ")";
                } else {
                    uid = "mock-fallback-" + idToken.hashCode();
                    email = idToken.contains("@") ? idToken : idToken + "@test.com";
                    name = "Mock Donor";
                }
            }

            if (uid != null) {
                final String finalEmail = email;
                final String finalName = name;
                final String finalUid = uid;
                User user = userRepository.findById(uid).orElseGet(() -> {
                    User newUser = new User();
                    newUser.setId(finalUid);
                    newUser.setEmail(finalEmail);
                    newUser.setName(finalName);
                    if ("admin@ngo.org".equalsIgnoreCase(finalEmail) || "mock-admin-uid".equals(finalUid)) {
                        newUser.setRole("ADMIN");
                    } else {
                        newUser.setRole("USER");
                    }
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

                UserPrincipal principal = new UserPrincipal(user);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            log.error("Firebase Authentication failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired authentication token.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
