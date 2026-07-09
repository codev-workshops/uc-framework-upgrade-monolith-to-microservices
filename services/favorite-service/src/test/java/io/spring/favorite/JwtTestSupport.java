package io.spring.favorite;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public final class JwtTestSupport {
  public static final String SECRET =
      "nRvyYC4soFxBdZ-F-5Nnzz5USXstR1YylsTd-mA0aKtI9HUlriGrtkf-TiuDapkLiUCogO3JOK7kwZisrHp6wA";

  private JwtTestSupport() {}

  public static String tokenFor(String userId) {
    SignatureAlgorithm alg = SignatureAlgorithm.HS512;
    SecretKey key = new SecretKeySpec(SECRET.getBytes(), alg.getJcaName());
    return Jwts.builder()
        .setSubject(userId)
        .setExpiration(new Date(System.currentTimeMillis() + 86400_000L))
        .signWith(key)
        .compact();
  }
}
