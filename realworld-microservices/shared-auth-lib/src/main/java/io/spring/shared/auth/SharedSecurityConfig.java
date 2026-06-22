package io.spring.shared.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SharedSecurityConfig {
    @Bean
    public JwtService sharedJwtService(@Value("${jwt.secret}") String secret) {
        return new DefaultJwtService(secret);
    }

    @Bean
    public JwtTokenFilter sharedJwtTokenFilter(JwtService jwtService) {
        return new JwtTokenFilter(jwtService);
    }
}
