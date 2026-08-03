package com.codearena.security;

import com.codearena.user.User;
import com.codearena.user.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        extractToken(request).ifPresent(token -> authenticate(token, request));
        filterChain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }

    private void authenticate(String token, HttpServletRequest request) {
        if (!jwtTokenProvider.isValid(token)) {
            return;
        }
        try {
            Claims claims = jwtTokenProvider.parseClaims(token);
            if ("refresh".equals(claims.get("type"))) {
                return; // refresh tokens may only be used at /auth/refresh, never as a bearer token
            }
            String userId = claims.getSubject();
            User user;
            try {
                user = userService.getById(userId);
            } catch (Exception e) {
                return;
            }
            if (!user.isEnabled()) {
                return;
            }
            UserPrincipal principal = new UserPrincipal(user);
            var authentication = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            log.debug("Could not authenticate JWT: {}", ex.getMessage());
        }
    }
}
