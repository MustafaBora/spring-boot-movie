package org.hyf.movie.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtils {

    //decoder
    //encoder
    //generate token
    //verify token
    //parse
    //read the token and get info about it
    private final Key key;
    private final long expiration;

    JwtUtils(@Value("${app.jwt.secret}")String secret, @Value("${app.jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    private Jws<Claims> parse(String token) {
        //TODO this throws MalformedJwtException if there is no JWT
        return Jwts.parserBuilder().setSigningKey(key)
                .build().parseClaimsJws(token);
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expire = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)  //provide the payload of the JWT
                .setSubject(subject)    // it is used to identify the user
                .setIssuedAt(now)       //set the issued at
                .setExpiration(expire)  //set the expiration
                .signWith(key)              //TODO decode when we set the key with base64 encoding
                .compact();
    }

    public boolean isValid(String token) {  //we may have to handle exceptions because of parsing
        Date tokenExpiration = parse(token).getBody().getExpiration();
        return tokenExpiration.after(new Date());
    }

    public String getSubject(String token) {
        Claims body = parse(token).getBody();
        //TODO check it in the debugger
        return body.getSubject();
    }

}
