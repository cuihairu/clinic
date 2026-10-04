package com.clinic.service.impl;

import com.clinic.entity.StaffEntity;
import com.clinic.security.UserPrincipal;
import com.clinic.service.StaffService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements LogoutHandler, UserDetailsService {

    private final StaffService staffService;
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        log.info("logout:{}", authHeader);
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty() || username.isBlank()) {
            throw new UsernameNotFoundException("username is empty");
        }
        log.info("loadUserByUsername:{}", username);
        var staff = staffService.findByAccount(username).orElseThrow(()->new UsernameNotFoundException(username + " user not found"));
        return new UserPrincipal(staff);
    }
}
