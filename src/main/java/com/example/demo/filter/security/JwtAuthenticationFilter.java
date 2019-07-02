package com.example.demo.filter.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.config.AppProperties;
import com.example.demo.config.security.JwtAuthenticationToken;
import com.example.demo.service.MyUserDetailsService;
import com.example.demo.utils.JwtUtils;
import com.example.demo.utils.SpringUtils;
import com.google.common.collect.Lists;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.HttpSecurityBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestHeaderRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 也可以重写 org.springframework.security.web.authentication.www.BasicAuthenticationFilter来实现
 * @see BasicAuthenticationFilter
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    List<RequestMatcher> permissiveRequests;
    private AppProperties appProperties;
    private String TOEKN_HEADER;
    private RequestMatcher requestHeadMatcher;
    private AuthenticationManager authenticationManager;

    private AuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
    private AuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler();

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager){
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        Assert.notNull(authenticationManager, "authenticationManager 未定义,请检查");
        Assert.notNull(successHandler, "successHandler 未定义,请检查");
        Assert.notNull(failureHandler, "failureHandler 未定义,请检查");
    }

    public JwtAuthenticationFilter(){
        AppProperties properties = SpringUtils.getBean(AppProperties.class);
        this.appProperties = properties;
        this.TOEKN_HEADER  = properties.getSecurity().getTokenHeader();
        requestHeadMatcher = new RequestHeaderRequestMatcher(TOEKN_HEADER);
    }
    public JwtAuthenticationFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
        TOEKN_HEADER = appProperties.getSecurity().getTokenHeader();
    }

    protected boolean requiresAuthentication(HttpServletRequest request) {
        return requestHeadMatcher.matches(request);
    }

    protected String getJwtToken(HttpServletRequest request) {
        String authInfo = request.getHeader(TOEKN_HEADER);
        return StringUtils.removeStart(authInfo, "Bearer ");
    }

    /**
     * 校验head 中的token
     * @param request
     * @param response
     * @param chain
     * @throws IOException
     * @throws ServletException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 不需要验证的就跳过
        if (!requiresAuthentication(request)) {
            chain.doFilter(request, response);
            return;
        }
        AuthenticationException authenticationException =null;
        Authentication authResult = null;
        try {
            String token = getJwtToken(request);
            // header 不为空
            if(StringUtils.isNotBlank(token)){
                DecodedJWT decodedJWT = JWT.decode(token);
                JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(decodedJWT);
                authResult = this.getAuthenticationManager().authenticate(authenticationToken);
            }else{
                logger.error("token is null");
                authenticationException= new InsufficientAuthenticationException("token 不能为空");
            }
        }catch (JWTDecodeException e){
            logger.error("token 解析失败");
            authenticationException = new InsufficientAuthenticationException("token 解析失败",authenticationException);
        }catch (InternalAuthenticationServiceException e){
            logger.error("An internal error occurred while trying to authenticate the user.", e);
            authenticationException = e;
        }catch (AuthenticationException e){
            logger.error("token 认证失败.", e);
            authenticationException = e;
        }
        if(authResult != null){
            /**
             * 认证成功后的回调
             * {@link com.example.demo.config.security.JwtVaildSuccessHandler}
             * @see com.example.demo.config.security.JwtAuthenticationConfigurer#successHandler(AuthenticationSuccessHandler)
             * @see com.example.demo.config.security.SecurityConfig#configure(HttpSecurity)
             */
            onSuccessfulAuthentication(request,response,authResult);
        } else if(!this.permissiveRequest(request)){
            /**
             * 认证失败后的回调
             * {@link com.example.demo.config.security.JwtAccessDeniedhandler}
             * @see com.example.demo.config.security.JwtAuthenticationConfigurer#configure(HttpSecurityBuilder)
             */
            onUnsuccessfulAuthentication(request,response,authenticationException);
            return;
        }
        chain.doFilter(request, response);
    }

    /**
     * token校验成功的回调函数
     *
     */
    protected void onSuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response, Authentication authResult) throws IOException, ServletException {
       SecurityContextHolder.getContext().setAuthentication(authResult);
       successHandler.onAuthenticationSuccess(request,response,authResult);
    }

    // token校验失败的回调函数
    protected void onUnsuccessfulAuthentication(HttpServletRequest request,
                                                HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        failureHandler.onAuthenticationFailure(request,response,failed);
    }

    public void setPermissiveRequests(String... urls) {
        if(this.permissiveRequests == null){
            permissiveRequests = new ArrayList<>();
        }
        permissiveRequests.addAll(Arrays.stream(urls).map(AntPathRequestMatcher::new).collect(Collectors.toList()));
    }

    public boolean permissiveRequest(HttpServletRequest request){
        return CollectionUtils.isEmpty(this.permissiveRequests) ? false : this.permissiveRequests.stream().anyMatch(m->m.matches(request));
    }

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setSuccessHandler(AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
    }

    public void setFailureHandler(AuthenticationFailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }
}
