package com.yilinglin10.authservice.util;

import com.yilinglin10.authservice.exception.AuthenticateException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.jackson.io.JacksonDeserializer;
import io.jsonwebtoken.lang.Maps;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

import static io.jsonwebtoken.SignatureAlgorithm.HS256;

@Component
@Slf4j
public class JwtUtils {
    private static final String SECRET = "703273357638792F423F4528482B4D6251655468576D5A7134743677397A2443";
    // expiration time: 30 minutes
    private static final long EXPIRATION_TIME = 30*60*1000;

    @Value("${jwt.claims.authorities.key}")
    public String AUTHORITIES_KEY;

    @Value("${jwt.claims.userId.key}")
    public String USER_ID_KEY;

    public String generateToken(String username, Long userId, List<String> authorities) {
        return Jwts.builder()
                .setSubject(username)
                .claim(AUTHORITIES_KEY, authorities)
                .claim(USER_ID_KEY, userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSignKey(), HS256)
                .compact();
    }

    public void validateToken(final String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSignKey()).build().parse(token);
        } catch (SignatureException e) {
            throw new AuthenticateException("Invalid JWT signature");
        } catch (MalformedJwtException e) {
            throw new AuthenticateException("Invalid JWT token");
        } catch (ExpiredJwtException e) {
            throw new AuthenticateException("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            throw new AuthenticateException("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            throw new AuthenticateException("JWT claims string is empty");
        }
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get(USER_ID_KEY, Long.class);
    }

    public List<String> extractAuthorities(String token) {
        return Arrays.asList(extractAllClaims(token).get(AUTHORITIES_KEY, String[].class));
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
//                https://github.com/jwtk/jjwt#parsing-of-custom-claim-types
                .deserializeJsonWith(new JacksonDeserializer( Maps.of(AUTHORITIES_KEY, String[].class).build()))
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
