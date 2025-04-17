package com.bot.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

public class GuiApp extends Application {
    private static ConfigurableApplicationContext springContext;

    private Stage stageAll = new Stage();

    public static void setSpringContext(ApplicationContext context) {
        GuiApp.springContext = (ConfigurableApplicationContext) context;
    }

    public void start(Stage primaryStage) throws IOException {
//        maskToolView();

        compareToolView();
        //關閉介面同時關閉執行序
        close();
    }

    /**
     * 遮蔽工具啟動畫面
     */
    private void maskToolView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        loader.setControllerFactory(springContext::getBean); // 結合 Spring

        //介面大小
        Scene scene = new Scene(loader.load(), 250, 100);

        stageAll.setScene(scene);

        stageAll.setTitle("遮蔽資料工具");

        stageAll.show();

    }

    private void compareToolView() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CompareView.fxml"));
        loader.setControllerFactory(springContext::getBean); // 結合 Spring

        Scene scene = new Scene(loader.load(),1000,600);
        stageAll.setTitle("CompareView");
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
