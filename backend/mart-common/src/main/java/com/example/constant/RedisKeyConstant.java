package com.example.constant;

/**
 * Redis key 前缀常量
 */
public class RedisKeyConstant {

    /**
     * 短信验证码：campus:sms:code:{phone}
     */
    public static final String SMS_CODE = "campus:sms:code:%s";

    /**
     * 用户 token 黑名单：campus:token:blacklist:{token}
     */
    public static final String TOKEN_BLACKLIST = "campus:token:blacklist:%s";
}