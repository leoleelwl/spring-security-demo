package com.example.demo.config.security.login;

import com.example.demo.filter.security.MyUsernamePasswordAuthenticationFilter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

public class LoginConfigurer<T extends LoginConfigurer<T, B>, B extends HttpSecurityBuilder<B>> extends AbstractHttpConfigurer<T,B> {

    MyUsernamePasswordAuthenticationFilter usernamePasswordAuthenticationFilter;

    public LoginConfigurer(){
        this.usernamePasswordAuthenticationFilter = new MyUsernamePasswordAuthenticationFilter();
    }
    @Override
    public void configure(B http) throws Exception {
        usernamePasswordAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        usernamePasswordAuthenticationFilter.setAuthenticationFailureHandler(new LoginFailureHandler());
        usernamePasswordAuthenticationFilter.setSessionAuthenticationStrategy(new NullAuthenticatedSessionStrategy());
        MyUsernamePasswordAuthenticationFilter postProcessFilter = postProcess(usernamePasswordAuthenticationFilter);
        http.addFilterAfter(postProcessFilter, LogoutFilter.class);
    }

    public LoginConfigurer<T,B> loginSuccessHandler(AuthenticationSuccessHandler successHandler){
        usernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(successHandler);
        return this;
    }
}
