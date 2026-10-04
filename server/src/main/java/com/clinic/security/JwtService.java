package com.clinic.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

@Component
public class JwtService {
    public static String Header = "Authorization";
    public static String RefreshHeader = "Refresh";
    public static String Prefix = "Bearer ";
    public static String UserPrincipal = "userPrincipal";
    public static String Name = "jwt";
    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    private SecretKey key;
    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        if (jwtExpiration < 300L){
            jwtExpiration = 300L;
        }
        return generateToken(new HashMap<>(), userDetails,jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        if (refreshExpiration < jwtExpiration*2){
            refreshExpiration = jwtExpiration*2;
        }
        return generateToken(new HashMap<>(), userDetails,refreshExpiration);
    }

    public String withPrefix(String token){
        return Prefix+token;
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails, Long expiration
    ) {
        return buildToken(extraClaims, userDetails, Math.max(expiration,300L));
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder().claims().empty().add(extraClaims).and()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
    }

    private SecretKey getSignInKey() {
        if (key != null) {
            return key;
        }
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        key = Keys.hmacShaKeyFor(keyBytes);
        return key;
    }
}
