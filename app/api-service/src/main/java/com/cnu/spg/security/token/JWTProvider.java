package com.cnu.spg.security.token;


import com.cnu.spg.user.domain.UserPrincipal;
import com.cnu.spg.user.exception.UserTypeIsNotValid;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JWTProvider implements TokenProvider {
    public static final String CLAIM_ID_KEY = "userId";

    @Value("${jwt.token.secret.key}")
    private String tokenSecretKey;

    @Value("${jwt.token.expiration.seconds}")
    private int expireTime;

    @Value("${jwt.http.request.header}")
    private String tokenHeaderName;

    @Override
    public String createToken(UserDetails userDetails) {
        if (!(userDetails instanceof UserPrincipal)) {
            throw new UserTypeIsNotValid(userDetails.getClass().getName());
        }

        Date now = new Date();
        Date expireDate = createExpireTime(now);
        Map<String, Object> payload = new HashMap<>();
        payload.put(CLAIM_ID_KEY, ((UserPrincipal) userDetails).getId());

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setExpiration(expireDate)
                .setClaims(payload)
                .signWith(SignatureAlgorithm.HS256, tokenSecretKey)
                .compact();
    }

    private Date createExpireTime(Date start) {
        return new Date(start.getTime() + expireTime);
    }

    @Override
    public String getTokenHeaderName() {
        return tokenHeaderName;
    }

    @Override
    public String getTokenSubject(String token) {
        String username = Jwts.parser().setSigningKey(tokenSecretKey)
                .parseClaimsJws(token).getBody()
                .getSubject();
        if (username == null || username.isEmpty()) {
            return null;
        }

        return username;
    }

    @Override
    public Map<String, Object> getTokenPayload(String token) {
        return Jwts.parser()
                .setSigningKey(tokenSecretKey) // Set Key
                .parseClaimsJws(token) // 파싱 및 검증, 실패 시 에러
                .getBody();

    }
}