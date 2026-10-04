package com.clinic.security;

import com.clinic.entity.StaffEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@NoArgsConstructor
public class UserPrincipal implements UserDetails {
    private static final long serialVersionUID = 1L;
    private StaffEntity staff;

    public UserPrincipal(StaffEntity staff) {
        this.staff = staff;
    }

    public StaffEntity getStaff() {
        return this.staff;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.staff.getAuthorities();
    }

    @Override
    public String getPassword() {
        return this.staff.getPassword();
    }

    @Override
    public String getUsername() {
        return this.staff.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.staff.isEnabled();
    }
}
