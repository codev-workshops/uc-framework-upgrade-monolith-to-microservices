package io.spring;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/** Mints HS512 JWTs identical to user-auth-service for use in tests. */
public final class TestJwt {
  public static final String SECRET =
      "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA";

  private TestJwt() {}

  public static String tokenFor(String userId) {
    SecretKey key = new SecretKeySpec(SECRET.getBytes(), SignatureAlgorithm.HS512.getJcaName());
    return Jwts.builder()
        .setSubject(userId)
        .setExpiration(new Date(System.currentTimeMillis() + 86400_000L))
        .signWith(key)
        .compact();
  }
}
