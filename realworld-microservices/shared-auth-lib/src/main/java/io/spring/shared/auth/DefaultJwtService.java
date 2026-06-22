package io.spring.shared.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Optional;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class DefaultJwtService implements JwtService {
    private final SecretKey signingKey;

    public DefaultJwtService(String secret) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS512;
        this.signingKey = new SecretKeySpec(secret.getBytes(), signatureAlgorithm.getJcaName());
    }

    @Override
    public Optional<String> getSubFromToken(String token) {
        try {
            Jws<Claims> claimsJws =
                    Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token);
            return Optional.ofNullable(claimsJws.getBody().getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
