package com.clinic.controller;


import com.clinic.entity.StaffEntity;
import com.clinic.params.AuthParams;
import com.clinic.service.JwtService;
import com.clinic.service.StaffService;
import com.clinic.vo.LoginView;
import com.clinic.vo.Resp;
import com.clinic.vo.UserView;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户", description = "后台用户")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final StaffService staffService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public LoginView Register(@Validated @RequestBody AuthParams authParams){
        StaffEntity staffEntity = staffService.create(authParams.getUsername().trim(), authParams.getPassword().trim());
        String token = jwtService.generateToken(staffEntity);
        return LoginView.builder().token(token).build();
    }

    @PostMapping("/login")
    public LoginView Login(@Validated @RequestBody AuthParams authParams, HttpServletResponse response) throws IllegalArgumentException{
        StaffEntity staffEntity = staffService.findByAccount(authParams.getUsername().trim()).orElseThrow(() -> new IllegalArgumentException("该用户不存在"));
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authParams.getUsername(),authParams.getPassword().trim()));
        String token = jwtService.generateToken(staffEntity);
        response.setHeader(JwtService.Header,jwtService.withPrefix(token));
        Cookie cookie = new Cookie("jwt", token);
        response.addCookie(cookie);
        return LoginView.builder().status("ok").type("account").token(token).currentAuthority(staffEntity.getRole()==0?"admin":"user").build();
    }

    @PostMapping("/logout")
    public LoginView Logout(){
        return LoginView.builder().build();
    }
    @GetMapping("/current")
    public UserView current(HttpServletRequest request){
        StaffEntity currentStaff = (StaffEntity)request.getSession().getAttribute("currentStaff");
        return UserView.FromStaffEntity(currentStaff);
    }
}
