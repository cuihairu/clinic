package com.clinic.security;

import com.clinic.service.JwtService;
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
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    //private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Methods", "POST,GET,OPTIONS,DELETE,PUT,HEAD,PATCH");
        response.addHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.addHeader("Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers"));
        if (request.getServletPath().contains("/api/v1/user/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader(JwtService.Header);
        String jwt = null;
        final String account;
        if (authHeader != null && authHeader.startsWith(JwtService.Prefix)) {
            jwt = authHeader.substring(7);
        }else{
            Cookie[] cookies = request.getCookies();
            if (cookies == null || cookies.length == 0) {
                filterChain.doFilter(request, response);
                return;
            }
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(JwtService.Name)){
                    jwt = cookie.getValue();
                    break;
                }
            }
        }
        if (jwt == null || jwt.isEmpty()){
            filterChain.doFilter(request, response);
            return;
        }
        account = jwtService.extractUsername(jwt);
        if (account != null && SecurityContextHolder.getContext().getAuthentication() == null) {
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
                request.getSession().setAttribute("currentStaff",userDetails);
            }
        }
        filterChain.doFilter(request, response);
    }
}
