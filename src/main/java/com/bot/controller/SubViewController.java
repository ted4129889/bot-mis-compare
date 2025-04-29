package com.bot.controller;

import com.bot.util.log.LogProcess;
import com.bot.service.mask.MaskDataBaseService;
import com.bot.service.mask.MaskRunSqlService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SubViewController {

    @FXML
    private ComboBox<String> envComboBox;
    @FXML
    private DatePicker datePicker;


    public Button btnMaskData = new Button();
    public Button btnMaskRunSql = new Button();

    public String dateParam = "";
    //    public Button btnMaskDataFile = new Button();

    @Value("${localFile.mis.batch.output}")
    private String allowedPath;

    @Autowired
    private MaskDataBaseService maskDataBaseService;

    @Autowired
    private MaskRunSqlService maskRunSqlService;

    @Autowired
//    private MaskDataFileService maskDataFileService;

    @FXML
    public void initialize() {
//        int cores = Runtime.getRuntime().availableProcessors();
//       LogProcess.info("Core Count: " + cores);
        envComboBox.getItems().addAll("prod", "dev", "local");
//        , "MaskFileData"
        envComboBox.setValue("prod");

        // 一開始根據預設值設定按鈕狀態
        updateButtonVisibility(envComboBox.getValue());

        // 當 ComboBox 值改變時，更新按鈕顯示
        envComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonVisibility(newVal);
        });
    }

    private void updateButtonVisibility(String env) {
        //切換環境
        LogProcess.info("目前環境:" +env);


        switch (env) {
            case "prod":
                btnMaskData.setText("執行產生遮蔽SQL檔");
                btnMaskData.setVisible(true);
                btnMaskRunSql.setVisible(false);
                break;
            case "dev":
            case "local":
                List<String> sqlPaths = maskRunSqlService.getSafeSQLFilePaths(allowedPath);
                btnMaskData.setText("執行產生遮蔽SQL檔");
                btnMaskData.setVisible(true);
                btnMaskRunSql.setVisible(true);
                if(sqlPaths.isEmpty()){
                    btnMaskRunSql.setDisable(true);
                }else{
                    btnMaskRunSql.setDisable(false);
                }

                break;
            case "MaskFileData":
                btnMaskData.setText("執行資料檔案遮蔽");
                btnMaskData.setVisible(true);
                btnMaskRunSql.setVisible(false);
                break;
        }

    }

    @FXML
    private void btnMaskData() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            String date = selectedDate.toString().replace("-","");
//            showAlert("選擇的日期: ",date);
            String env = envComboBox.getValue();
            boolean flag = false;
            switch (env) {
                case "MaskFileData":
//                    flag = maskDataFileService.exec();
                    break;
                default:
                    flag = maskDataBaseService.exec(env,date);
                    break;
            }
            runWithAlert(flag, env);

            btnMaskRunSql.setDisable(false);


        } else {
            showAlert("", "請先選擇資料日期");
        }
    }

    @FXML
    private void btnMaskRunSql() {

        String env = envComboBox.getValue();
        boolean flag = maskRunSqlService.exec(env);
        runWithAlert(flag, env);
    }

    private void runWithAlert(boolean flag, String env) {

        String successMsg = "執行完成";
        String failMsg = "";
        switch (env) {
            case "prod":
                failMsg = "請到測試環境使用";
                break;
            default:
                //dev、local
                failMsg = "請到正式環境使用";
                break;
        }

        showAlert("信息", flag ? successMsg : failMsg);

    }


    /**
     * 顯示彈出提示框
     */
    public void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setPrefSize(100, 40);
        alert.showAndWait();
    }


}
