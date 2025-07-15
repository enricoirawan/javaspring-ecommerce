package com.fastcampus.ecommerce.service;

import com.fastcampus.ecommerce.common.DateUtil;
import com.fastcampus.ecommerce.config.JwtSecretConfig;
import com.fastcampus.ecommerce.model.UserInfo;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final JwtSecretConfig jwtSecretConfig;
    private final SecretKey signKey;

    @Override
    public String generateToken(UserInfo userInfo) {
        LocalDateTime expirationTime = LocalDateTime.now().plus(jwtSecretConfig.getJwtExpirationTime());
        Date expirationDate = DateUtil.convertLocalDateTimeToDate(expirationTime);

        return Jwts.builder()
                .setSubject(userInfo.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expirationDate)
                .signWith(signKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(signKey)
                    .build();
            parser.parseClaimsJws(token);

            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(signKey)
                .build();
        return parser.parseClaimsJws(token)
                .getBody()
                .getSubject(); //line 30
    }
}