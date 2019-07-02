package com.example.demo.config.security;

import com.example.demo.filter.security.JwtAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;

public class JwtAuthenticationConfigurer<T extends JwtAuthenticationConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T,B> {

    JwtAuthenticationFilter jwtAuthenticationFilter;

    public JwtAuthenticationConfigurer(){
        this.jwtAuthenticationFilter = new JwtAuthenticationFilter();
    }
    @Override
    public void configure(B http){
        jwtAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        jwtAuthenticationFilter.setFailureHandler(new JwtAccessDeniedhandler());
        JwtAuthenticationFilter filter = this.postProcess(this.jwtAuthenticationFilter);
        http.addFilterBefore(filter, LogoutFilter.class);
    }

    public JwtAuthenticationConfigurer<T,B> permissiveUrls(String... urls){
        jwtAuthenticationFilter.setPermissiveRequests(urls);
        return this;
    }

    public JwtAuthenticationConfigurer<T,B> successHandler(AuthenticationSuccessHandler successHandler){
        jwtAuthenticationFilter.setSuccessHandler(successHandler);
        return this;
    }
}
