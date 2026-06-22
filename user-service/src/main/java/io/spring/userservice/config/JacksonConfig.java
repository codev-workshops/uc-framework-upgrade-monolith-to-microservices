package io.spring.userservice.config;

import io.spring.common.jackson.JacksonCustomizations;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(JacksonCustomizations.class)
public class JacksonConfig {}
