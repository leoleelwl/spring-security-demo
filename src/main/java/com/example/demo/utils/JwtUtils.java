package com.example.demo.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.InvalidClaimException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.demo.config.AppProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Date;

@Slf4j
public class JwtUtils {

    private static Long TOEKN_EXPIRE_TIME = SpringUtils.getBean(AppProperties.class).getSecurity().getJwtExpireAt();

    /**
     * 生成token
     * @param username 用户名
     * @param salt    盐值
     * @return
     */
    public static String sign(String username,String salt){
        username = StringUtils.lowerCase(username);
        Algorithm algorithm = Algorithm.HMAC256(salt);
        Date expireDate = new Date(System.currentTimeMillis()+TOEKN_EXPIRE_TIME*1000);
        //Date expireDate = new Date(System.currentTimeMillis()+10000);
        return JWT.create()
                .withIssuedAt(new Date())
                .withSubject(username)
                .withClaim("username", username)
                .withExpiresAt(expireDate)
                .sign(algorithm);
    }

    /**
     * 从 token中获取用户名
     * @return token中包含的用户名
     */
    public static String getUsername(String token){
        try {
            DecodedJWT decode = JWT.decode(token);
            return decode.getClaim("username").asString();
        }catch (JWTDecodeException e){
            log.error("jwt 解析出错 token:[{}]",token);
            return null;
        }
    }

    /**
     * 校验用户
     * @param username 用户名
     * @param salt    盐值
     * @param token
     * @return
     */
    public static boolean verify(String username,String salt,String token){
        try {
            Algorithm algorithm = Algorithm.HMAC256(salt);
            JWTVerifier verifier =
                    JWT.require(algorithm).withClaim("username", username).build();
            verifier.verify(token);
            log.info("token 校验正确");
            return true;
        }catch (TokenExpiredException e){
            log.info("token 已过期 , 用户:{} ,token [{}]",username,token);
            return false;
        }catch (InvalidClaimException e){
            log.info("非法的用户: {}",username);
            return false;
        }catch (Exception e){
            log.info("校验jwt失败: 用户:{} ,token [{}]",username,token);
            return false;
        }
    }
}
