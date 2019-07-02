package com.example.demo.config.security;

import com.example.demo.config.AppProperties;
import com.example.demo.config.security.login.LoginConfigurer;
import com.example.demo.config.security.login.LoginFailureHandler;
import com.example.demo.config.security.login.LoginSuccessHandler;
import com.example.demo.config.security.logout.TokenClearLogoutHandler;
import com.example.demo.service.JwtAuthenticationProvider;
import com.example.demo.service.MyUserDetailsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.header.Header;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;


@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private MyUserDetailsService myUserDetailsService;

    @Autowired
    private AppProperties properties;

    @Autowired
    private LoginSuccessHandler loginSuccessHandler;

    @Autowired
    private JwtVaildSuccessHandler tokenVaildSuccessHandler;

    @Autowired
    private TokenClearLogoutHandler tokenClearLogoutHandler;

    @Autowired
    private ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Autowired
    Http401AuthenticationHandler http401AuthenticationHandler;

    @Bean("myAuthenticationManagerBean")
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        Security security = properties.getSecurity();
        String pUrls = security.getPermitUrl();
        boolean csrf = security.isEnableCsrf();
        String formLoginUrl = security.getFormLoginUrl();
        String headValue = security.getTokenHeader();
        // 使用 JWT，关闭token
        http
            .formLogin().loginPage(formLoginUrl).permitAll()
//          .successHandler(loginSuccessHandler).defaultSuccessUrl("/login")
            .and()
            .logout().logoutUrl("/logout").logoutSuccessUrl("/login")
            .addLogoutHandler(tokenClearLogoutHandler)
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler())
            .permitAll();
        //关闭csrf  跨域伪造
        if(!csrf){
            http.csrf().disable();
        }

        if(StringUtils.isNotEmpty(pUrls)){
            String[] urls = StringUtils.splitByWholeSeparatorPreserveAllTokens(pUrls, ",");
            http.authorizeRequests().antMatchers(urls).permitAll();
        }
        /*session配置*/
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests().anyRequest().authenticated();
        /* 自定义filter配置*/
        http/*.addFilter(myUsernamePasswordAuthenticationFilter())*/
            /*.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)*/
                .apply(new JwtAuthenticationConfigurer<>()).successHandler(tokenVaildSuccessHandler).permissiveUrls(new String []{"/logout"})
                .and()
                .apply(new LoginConfigurer<>()).loginSuccessHandler(loginSuccessHandler);
        /* 跨域头 */
        http
            .cors()
            .and()
            .headers().addHeaderWriter(new StaticHeadersWriter(
                Arrays.asList(new Header("Access-control-Allow-Origin","*"),
                              new Header("Access-Control-Expose-Headers",headValue))
        ));
        /*设置无权访问的handler*/
        http.exceptionHandling()
                .accessDeniedHandler(apiAccessDeniedHandler)
                .authenticationEntryPoint(http401AuthenticationHandler);
        http.anonymous();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(myUserDetailsService)
                .passwordEncoder(passwordEncoder())
                .and()
                /* 注册两个provider 用于用户密码验证和 jwt的校验*/
                .authenticationProvider(jwtAuthenticationProvider())
                .authenticationProvider(daoAuthenticationProvider());
    }


    /**
     * use {@link DelegatingPasswordEncoder} which supportspassword upgrades.
     */
    @Bean
   PasswordEncoder passwordEncoder(){
        // 使用密码前缀来标注加密类型 推荐 ${bcrypt}123456qwe
       return PasswordEncoderFactories.createDelegatingPasswordEncoder();
   }

   @Bean("jwtAuthenticationProvider")
   JwtAuthenticationProvider jwtAuthenticationProvider(){
       return new JwtAuthenticationProvider(myUserDetailsService);
   }

   @Bean("daoAuthenticationProvider")
   DaoAuthenticationProvider daoAuthenticationProvider(){
       DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
       provider.setUserDetailsService(myUserDetailsService);
       return provider;
   }

    @Bean
    protected CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.singletonList(CorsConfiguration.ALL));
        configuration.setAllowedMethods(Arrays.asList(HttpMethod.GET.name(),HttpMethod.HEAD.name(),HttpMethod.POST.name(), HttpMethod.OPTIONS.name()));
        configuration.setAllowedHeaders(Collections.singletonList(CorsConfiguration.ALL));
        configuration.addExposedHeader(properties.getSecurity().getTokenHeader());
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
