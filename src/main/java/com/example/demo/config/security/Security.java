package com.example.demo.config.security;

import lombok.Data;

@Data
public class Security {
    private boolean enableCsrf;
    private String permitUrl;
    private String formLoginUrl;
    /*token 过期时间 单位秒*/
    private Long jwtExpireAt = 3600L;
    private String tokenHeader = "Authorization";
}
