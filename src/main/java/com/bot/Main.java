package com.bot;


import com.bot.config.DecryptPasswordInitializer;
import com.bot.log.LogProcess;
import com.bot.ui.GuiApp;
import javafx.application.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.swing.*;
import java.awt.*;


@SpringBootApplication
public class Main {

    public static void main(String[] args) {

        SpringApplication app = new SpringApplication(Main.class);
        app.addInitializers(new DecryptPasswordInitializer());

        app.setLazyInitialization(true);  // 設置全局懶加載
//        app.run(args);
        ApplicationContext context = app.run(args);

        // 設置 Spring Context 給 JavaFX
        GuiApp.setSpringContext(context);

        // 執行JavaFX 應用
        Application.launch(GuiApp.class, args);


    }


}
