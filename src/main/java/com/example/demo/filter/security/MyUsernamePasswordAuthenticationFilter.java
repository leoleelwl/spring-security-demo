package com.example.demo.filter.security;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
public class MyUsernamePasswordAuthenticationFilter  extends UsernamePasswordAuthenticationFilter{

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 拿到requestbody里面的东西
            String body = StreamUtils.copyToString(request.getInputStream(), Charset.forName("UTF-8"));
            String username = null,password = null;
            if(StringUtils.hasText(body)){
                JSONObject userObj= JSON.parseObject(body);
                username = userObj.getString("username");
                password = userObj.getString("password");
            }else{
                username = obtainUsername(request);
                password = obtainPassword(request);
            }
            if(username==null){
                username = "";
            }
            if(password == null){
                password="";
            }
            username = username.trim();
            UsernamePasswordAuthenticationToken authRequest =
                    new UsernamePasswordAuthenticationToken(username,password);
            // Allow subclasses to set the "details" property
            return this.getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            log.error(" [method] attemptAuthentication 读取HttpServletRequest inputstream 失败");
        }
        return null;
    }
}
