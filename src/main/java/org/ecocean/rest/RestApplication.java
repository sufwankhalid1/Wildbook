package org.ecocean.rest;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.ecocean.ShepherdPMF;
import org.flywaydb.core.Flyway;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.samsix.database.ConnectionInfo;

@Configuration
@EnableAutoConfiguration
@ComponentScan
public class RestApplication extends SpringBootServletInitializer {
    /**
     *  This method should allow you to start up the rest service from a compiled jar rather
     *  than having to make a war and stick it in tomcat.
     */
    public static void main(final String[] args) {
        SpringApplication.run(RestApplication.class, args);
    }

    @Override
    protected final SpringApplicationBuilder configure(final SpringApplicationBuilder application) {
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.registerModule(new JodaModule());

        return application.sources(RestApplication.class);
    }

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException
    {
        //
        // WARN: DO NOT REMOVE THIS LINE
        //
        super.onStartup(servletContext);

        //
        // Uses default location of db/migration on classpath. You will find it in the src/main/resources folder.
        // OutOfOrder = true makes it so that if two developers create SQL in a different order and one somehow
        // gets applied to the database, the other's should too. This should only be an issue on development
        // databases and allows me to get other developers sql patches even if I've applied a newer one myself
        // locally. In production, everything should be fine.
        //
        ConnectionInfo connectionInfo = ShepherdPMF.getConnectionInfo();
        Flyway flyway = new Flyway();
        flyway.setOutOfOrder(true);
        flyway.setSqlMigrationPrefix("");
        flyway.setDataSource(connectionInfo.getUrl(), connectionInfo.getUserName(), connectionInfo.getPassword());
        flyway.migrate();
    }

    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        return new ServletRegistrationBean(new MediaUploadServlet(),"/mediaupload");
    }
}
