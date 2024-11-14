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

    /**
     * 生成token
     */
    public static String generateToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

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
