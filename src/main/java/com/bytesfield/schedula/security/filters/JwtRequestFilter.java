package com.bytesfield.schedula.security.filters;

import com.bytesfield.schedula.models.enums.JwtTokenType;
import com.bytesfield.schedula.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtRequestFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException, java.io.IOException {

        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(token, JwtTokenType.ACCESS);

            this.handleAccessTokenInvalidation(token, response);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                String cachedAccessToken = jwtService.getCachedAccessToken(username);

                if (cachedAccessToken == null || !cachedAccessToken.equals(token)) {
                    throw new JwtException("Token is invalid or expired");
                }

                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Check if the JWT is valid and if the user details are not null
                if (jwtService.isTokenValid(token, JwtTokenType.ACCESS, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (Exception ex) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }

    private void handleAccessTokenInvalidation(String token, HttpServletResponse response) throws java.io.IOException {
        String jwtId = jwtService.extractJwtId(token, JwtTokenType.ACCESS);

        if (jwtService.isAccessTokenInvalidated(jwtId)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated. Please login again.");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
}
