package com.example.demo.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.config.security.JwtAuthenticationToken;
import com.example.demo.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import java.util.Calendar;
import java.util.Date;

@Slf4j
public class JwtAuthenticationProvider implements AuthenticationProvider {


    MyUserDetailsService userDetailsService;

    public JwtAuthenticationProvider(MyUserDetailsService userDetailsService){
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
            DecodedJWT jwt = ((JwtAuthenticationToken) authentication).getToken();
            String token = jwt.getToken();
            Date expiresAt = jwt.getExpiresAt();
            if(expiresAt.before(Calendar.getInstance().getTime())){
                log.error("token 已过期");
                throw new NonceExpiredException("token 已过期");
            }
            String username = JwtUtils.getUsername(token);
            UserDetails userInfo = userDetailsService.getUserInfo(username);
            String salt = userInfo.getPassword();
            boolean verify = JwtUtils.verify(username, salt, token);
            if(!verify){
                throw new BadCredentialsException("token 校验失败");
            }
            JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(userInfo, jwt, userInfo.getAuthorities());
            return authenticationToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
