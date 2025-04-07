/* (C) 2024 */
package com.bot;

import java.awt.*;
import java.nio.file.Paths;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@SpringBootApplication
public class BatchApplication {
    public static void main(String[] args) {
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
            SpringApplication.run(BatchApplication.class, args);
        else {
            SpringApplication application = new SpringApplication(BatchApplication.class);
            application.addListeners(
                    new ApplicationPidFileWriter(Paths.get("").toAbsolutePath() + "/app.pid"));
            application.run();
        }
    }

    //        extends SpringBootServletInitializer {

    //    @Override
    //    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
    //        return builder.sources(getClass());
    //    }
    //
    //    public static void main(String[] args) {
    //        //        SpringApplication.run(BotApplication.class, args);
    //        System.setProperty("org.jboss.logging.provider", "slf4j");
    //        if (System.getProperty("os.name").toLowerCase().indexOf("windows") != -1)
    //            SpringApplication.run(BatchApplication.class, args);
    //        else {
    //            SpringApplication application = new SpringApplication(BatchApplication.class);
    //            application.addListeners(
    //                    new ApplicationPidFileWriter(Paths.get("").toAbsolutePath() +
    // "/app.pid"));
    //            application.run();
    //        }
    //    }

}
