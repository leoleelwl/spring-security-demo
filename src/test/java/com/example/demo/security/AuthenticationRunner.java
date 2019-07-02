package com.example.demo.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

public class AuthenticationRunner {

    public static void main(String[] args) {
        InMemoryUserDetailsManager userDetailsService = new InMemoryUserDetailsManager();
        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserDetails userDetails = User.withUsername("user")
                .password("{bcrypt}"+new BCryptPasswordEncoder().encode("password"))
                .roles("USER")
                .build();
        userDetailsService.createUser(userDetails);

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(encoder);
        provider.setUserDetailsService(userDetailsService);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("user","password");

        Authentication authenticate = provider.authenticate(token);

        System.out.println("是否已验证: "+authenticate.isAuthenticated());
        System.out.println("用户密码: "+authenticate.getCredentials());
        System.out.println("用户名: "+authenticate.getName());
        System.out.println("用户权限: "+authenticate.getAuthorities());

    }
}
