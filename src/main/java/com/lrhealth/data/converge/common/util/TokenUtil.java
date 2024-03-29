package com.lrhealth.data.converge.common.util;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * @author admin
 */
@Slf4j
public class TokenUtil {
    /**
     * 密钥
     */
    private static String SECRET_KEY = "secretKey";

    /**
     * 生成token
     */
    public static String generateToken(String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000);

        String token = Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();

        return token;
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
            if (claims.getExpiration().before(new Date())){
                return false;
            }
            return true;
        } catch (JwtException e) {
            log.error("token解析失败 e = {}",e.getMessage());
            return false;
        }
    }

    public static String parseJwtSubject(String jwtToken) {
        String subject = null;

        try {
            //Jws<Claims> claimsJws = Jwts.parser().parseClaimsJws(jwtToken);
            Claims claims = Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(jwtToken).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("subject解析失败 e = {}",e.getMessage());
        }
        return subject;
    }


}
