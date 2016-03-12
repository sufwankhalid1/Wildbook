package org.ecocean.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({"classpath:spring-context-${wildbook.cust}.xml"})
public class SpringConfig {
}
