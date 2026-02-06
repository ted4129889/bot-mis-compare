package com.bot;

import com.bot.config.DecryptPwdInitializer;
import com.bot.config.ExternalPropertiesInitializer;
import com.bot.ui.GuiApp;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

//exclude = {DataSourceAutoConfiguration.class 可不用啟動時自動加載資料庫連線
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//@SpringBootApplication
public class CompareApplication {

    public static void main(String[] args) {
//        ApplicationContext api = SpringApplication.run(CompareApplication.class, args);
//
//        JobContext ctx = new JobContext(Path.of("D:\\BOTProject\\com-bot-mis-compare\\batch-file\\newtest\\"),
//                Path.of("D:\\BOTProject\\com-bot-mis-compare\\batch-file\\newtest\\"),
//                "FasAlIfdDp"
//        );
//        // 單一行執行測試
//        api.getBean(JobExecutorService.class).runJob("FasAlIfdDp", ctx);
//
//        // 若要結束程式（避免 Spring 繼續跑 web server），可以關閉上下文
//        SpringApplication.exit(api);


        var configPath = Paths.get("external-parameters.properties");

        if (Files.notExists(configPath)) {
            try {
                Files.writeString(
                        configPath,
                        "SEPARATOR=;\n",
                        StandardOpenOption.CREATE_NEW
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



        SpringApplication app = new SpringApplication(CompareApplication.class);
        app.addInitializers(new DecryptPwdInitializer());


        ApplicationContext context = app.run(args);
        // 設置 Spring Context 給 JavaFX
        GuiApp.setSpringContext(context);

        // 執行JavaFX 應用
        Application.launch(GuiApp.class, args);

    }


}
