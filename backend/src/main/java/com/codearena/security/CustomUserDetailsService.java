package com.codearena.security;

import com.codearena.user.User;
import com.codearena.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserPrincipal loadUserByUsername(String email) {
        try {
            User user = userService.getByEmail(email);
            return new UserPrincipal(user);
        } catch (Exception e) {
            throw new UsernameNotFoundException("No account found for " + email);
        }
    }
}
