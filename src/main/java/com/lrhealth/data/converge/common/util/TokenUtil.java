package com.lrhealth.data.converge.common.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author admin
 */
@Slf4j
public class TokenUtil {

    private TokenUtil(){}
    /**
     * 密钥
     */
    private static final String secretKey = "secretKey";


    public static boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
            return !claims.getExpiration().before(new Date());
        } catch (JwtException e) {
            log.error("token解析失败 e = {}",e.getMessage());
            return false;
        }
    }

    public static String parseJwtSubject(String jwtToken) {
        String subject = null;

        try {
            Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(jwtToken).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("subject解析失败 e = {}",e.getMessage());
        }
        return subject;
    }


}
