package com.bot.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class GuiApp extends Application {
    private static ConfigurableApplicationContext springContext;

    private Stage stageAll = new Stage();

    public static void setSpringContext(ApplicationContext context) {
        GuiApp.springContext = (ConfigurableApplicationContext) context;
    }

    public void start(Stage primaryStage) throws IOException {
        stageAll = primaryStage;
        //檔案比對工具畫面
        compareToolView();
        //關閉介面同時關閉執行序
        close();
    }

    /**
     * 遮蔽工具啟動畫面
     */

    private void compareToolView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompareView.fxml"));
        loader.setControllerFactory(springContext::getBean); // 結合 Spring

        Scene scene = new Scene(loader.load(),900,550);
        stageAll.setTitle("Text File Comparison Tool");
        stageAll.setScene(scene);
        //禁止視窗調整
        stageAll.setResizable(false);
        stageAll.show();

    }


    private void close() {
        // 設置關閉事件：當視窗關閉時，關閉 Spring Boot 服務
        stageAll.setOnCloseRequest(event -> {
            springContext.close(); // 正常關閉 Spring Boot
            Platform.exit();       // 結束 JavaFX 應用
        });
    }

}
