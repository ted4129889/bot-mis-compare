package com.bot;

import com.bot.config.DecryptPwdInitializer;
import com.bot.ui.GuiApp;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class Main {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(Main.class);
        app.addInitializers(new DecryptPwdInitializer());

        ApplicationContext context = app.run(args);
        // 設置 Spring Context 給 JavaFX
        GuiApp.setSpringContext(context);

        // 執行JavaFX 應用
        Application.launch(GuiApp.class, args);


    }


}
