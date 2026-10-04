package com.clinic.security;

import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

@Service
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException ,SignatureException {
        final String authHeader = request.getHeader(JwtService.Header);
        if (authHeader != null && authHeader.startsWith(JwtService.Prefix)) {
            getUserPrincipal(request,authHeader);
        }
        filterChain.doFilter(request, response);
    }

    private void getUserPrincipal(@NonNull HttpServletRequest request,@NonNull String token) throws SignatureException {
        if (SecurityContextHolder.getContext().getAuthentication() != null){
            return;
        }
        String jwt = token.substring(7);
        if (jwt.isEmpty() || jwt.isBlank()){
            return;
        }
        String account = jwtService.extractUsername(jwt);
        if (account == null) {
            return;
        }
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(account);
        if (jwtService.isTokenValid(jwt, userDetails)) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );
            authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
            request.getSession().setAttribute(JwtService.UserPrincipal,userDetails);
        }
    }
}
