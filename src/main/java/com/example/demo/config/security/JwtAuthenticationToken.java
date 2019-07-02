package com.example.demo.config.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;


/**
 * @see UsernamePasswordAuthenticationToken
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private UserDetails userDetails;
    private DecodedJWT token;

    /***
     * 初始化的时候构造一个空的权限数组
     * @param token
     */
    public JwtAuthenticationToken(DecodedJWT token) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.token = token;
    }

    public JwtAuthenticationToken(UserDetails userInfo, DecodedJWT jwt, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.userDetails = userInfo;
        this.token = jwt;
        super.setAuthenticated(true); // must use super, as we override

    }

    @Override
    public Object getCredentials() {
        return this.token;
    }

    @Override
    public Object getPrincipal() {
        return this.userDetails;
    }

    public DecodedJWT getToken() {
        return token;
    }

    @Override
    public void setDetails(Object details) {
        super.setDetails(details);
        this.setAuthenticated(true);
    }

    public UserDetails getUserDetails() {
        return userDetails;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.userDetails = userDetails;
    }
}
