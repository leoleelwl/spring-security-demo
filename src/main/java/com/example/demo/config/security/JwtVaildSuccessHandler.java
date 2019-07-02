package com.example.demo.config.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.MyUserDetailsService;
import com.sun.scenario.effect.Offset;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;

@Component
public class JwtVaildSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    MyUserDetailsService userDetailsService;

    @Value("${app.security.tokenHeader}")
    private String TOKEN_HEADER;

    /* 30秒 */
    private long TOKEN_REFRESH_INTERVAL = 30L;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        DecodedJWT user = (DecodedJWT)authentication.getCredentials();
        Date issued = user.getIssuedAt();
        if(refreshToken(issued)){
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            String token = userDetailsService.saveUser(principal);
            response.setHeader(TOKEN_HEADER, token);
        }
        System.out.println("onAuthenticationSuccess");
    }

    public boolean refreshToken(Date date){
        LocalDateTime issueTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return LocalDateTime.now().minusSeconds(TOKEN_REFRESH_INTERVAL).isAfter(issueTime);
    }


    public static void main(String[] args) throws ParseException {

        String dateTime = "2019-06-24 16:56:01";
        //DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        //DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE;
        //LocalDateTime parse = LocalDateTime.parse(dateTime,formatter);
        //System.out.println(parse);
        /*System.out.println(
                Date.from(parse.atZone(ZoneId.systemDefault()).toInstant())
        );*/
        Date parseDate = DateUtils.parseDate(dateTime, "yyyy-MM-dd HH:mm:ss");

        System.out.println(parseDate);
//        String id = ZoneOffset.systemDefault().getId();
//        long second = LocalDateTime.now().toEpochSecond(ZoneOffset.of(ZoneOffset.systemDefault().getId()));
//        System.out.println(second);
    }
}
