package org.ecocean.rest;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
//@ImportResource({"classpath:cust.${WILDBOOK_CUST}-spring-context.xml"})
@ImportResource({"classpath:spring-context-${wildbook.cust.code}.xml"})
public class SpringConfig {
}
