package com.bot;

import com.bot.config.DecryptPwdInitializer;
import com.bot.dataprocess.JobContext;
import com.bot.dataprocess.JobExecutorService;
import com.bot.ui.GuiApp;
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;

import java.nio.file.Path;

//exclude = {DataSourceAutoConfiguration.class 可不用啟動時自動加載資料庫連線
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
//@SpringBootApplication
public class Main {

    public static void main(String[] args) {
//        ApplicationContext api = SpringApplication.run(Main.class, args);
//
//        JobContext ctx = new JobContext(Path.of("D:/BOTProject/com-bot-mis-compare/batch-file/input"),
//                Path.of("D:/BOTProject/com-bot-mis-compare/batch-file/output"),
//                "botsddb_20250930.dbo.db_achmr_dds"
//        );
//        // 單一行執行測試
//        api.getBean(JobExecutorService.class).runJob("DB_ACHMR", ctx);
//
//        // 若要結束程式（避免 Spring 繼續跑 web server），可以關閉上下文
//        SpringApplication.exit(api);

        SpringApplication app = new SpringApplication(Main.class);
        app.addInitializers(new DecryptPwdInitializer());

        ApplicationContext context = app.run(args);
        // 設置 Spring Context 給 JavaFX
        GuiApp.setSpringContext(context);

        // 執行JavaFX 應用
        Application.launch(GuiApp.class, args);
    }


}
