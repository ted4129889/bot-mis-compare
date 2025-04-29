package com.bot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import org.springframework.stereotype.Component;

@Component
public class ScrollSyncController {

    @FXML
    private ScrollPane scrollPane1;
    @FXML
    private ScrollPane scrollPane2;
    @FXML
    private TextArea textArea1;
    @FXML
    private TextArea textArea2;

    @FXML
    public void initialize() {
        // 設定等寬字型
        textArea1.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
        textArea2.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");

        // 模擬長內容
        textArea1.setText("這是左邊內容，內容很多所以會超出 → → → → → → → → → → → → → → → → → → → → → → → →\n第二行\n第三行");
        textArea2.setText("這是右邊內容，應該會同步滑動 → → → → → → → → → → → → → → → → → → → → → →\n第二行\n第三行");

        // 水平同步捲動
        scrollPane1.hvalueProperty().addListener((obs, oldVal, newVal) ->
                scrollPane2.setHvalue(newVal.doubleValue())
        );
        scrollPane2.hvalueProperty().addListener((obs, oldVal, newVal) ->
                scrollPane1.setHvalue(newVal.doubleValue())
        );
    }
}
