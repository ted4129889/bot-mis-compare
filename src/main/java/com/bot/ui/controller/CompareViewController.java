package com.bot.ui.controller;

import com.bot.compare.CompareDataService;
import com.bot.compare.CompareDataServiceImpl;
import com.bot.log.LogProcess;
import com.bot.mask.MaskDataFileService;
import com.bot.output.CompareFileExportImpl;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CompareViewController {
//    @Value("${localFile.mis.batch.compare.new.directory}")
//    private String newFilePath;
//    @Value("${localFile.mis.batch.compare.old.directory}")
//    private String oldFilePath;

    private String STR_BIG5 = "Big5";
    private String STR_UTF8 = "UTF-8";
    @Autowired
    MaskDataFileService maskDataFileService;
    @Autowired
    CompareFileExportImpl compareFileExportImpl;

    @FXML
    private Button btnLoadFile1;
    @FXML
    private Button btnLoadFile2;
    @FXML
    private Button btnCompare;

    @FXML
    private Label labelFile1;
    @FXML
    private Label labelFile2;
    @FXML
    private TextArea textArea1;
    @FXML
    private TextArea textArea2;
    @FXML
    private VBox fieldSelectionBox;
    @FXML
    private VBox pkSelectionBox;
    private File file1;
    private File file2;
    @FXML
    private ScrollPane scrollPane1;
    @FXML
    private ScrollPane scrollPane2;
    private File lastDirectory = null;

    int dataLength = 0;

    @FXML
    public void initialize() {
        //預設按鈕為不啟用
        btnCompare.setDisable(true);

        // 設定字型等寬，確保對齊
        textArea1.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");
        // 設定拖曳檔案事件
        textArea1.setOnDragOver(event -> {
            if (event.getGestureSource() != textArea1 &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        textArea1.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                // 只取第一個
                file1 = db.getFiles().get(0);
                //  顯示路徑
                labelFile1.setText(file1.getAbsolutePath());
                textArea1.setText(readFile(file1));
                maskDataFileService.exec(file1.getAbsolutePath(), "textarea1");
                checkFileExists();
                checkAndTriggerNext();

                success = true;
            }
            // 設置拖放完成狀態並消耗事件
            event.setDropCompleted(success);
            event.consume();
        });
        textArea2.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14;");

        textArea2.setOnDragOver(event -> {
            if (event.getGestureSource() != textArea2 &&
                    event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        textArea2.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                // 只取第一個
                file2 = db.getFiles().get(0);
                //  顯示路徑
                labelFile2.setText(file2.getAbsolutePath());
                textArea2.setText(readFile(file2));
                maskDataFileService.exec(file2.getAbsolutePath(), "textarea2");
                checkFileExists();
                checkAndTriggerNext();
                // 可以讀取它的內容或儲存 File 變數
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });


        // 水平同步捲動
        scrollPane1.hvalueProperty().addListener((obs, oldVal, newVal) ->
                scrollPane2.setHvalue(newVal.doubleValue())
        );
        scrollPane2.hvalueProperty().addListener((obs, oldVal, newVal) ->
                scrollPane1.setHvalue(newVal.doubleValue())
        );

    }

    /**
     * 檢查TextArea1 和 TextArea2 內容決定是否顯示checkBox
     */
    private void checkAndTriggerNext() {
        String text1 = textArea1.getText();
        String text2 = textArea2.getText();

        if (text1 != null && !text1.isBlank() && text2 != null && !text2.isBlank()) {
            textArea1.setPrefWidth(estimateTextWidth(dataLength, textArea1));
            textArea2.setPrefWidth(estimateTextWidth(dataLength, textArea2));
            columnsCheckBoxList();
        }
    }

    private void checkFileExists() {
        if (!maskDataFileService.fileExists()) {
            showAlert("檔案名稱不存在定義檔");
        }
    }

    @FXML
    public void loadFile1() {
        file1 = chooseFile();

        if (file1 != null) {
            String filePath = file1.getAbsoluteFile().toString();
            labelFile1.setText(filePath);
            textArea1.setText(readFile(file1));
            maskDataFileService.exec(filePath, "textarea1");
            checkFileExists();
            checkAndTriggerNext();
        }


    }

    @FXML
    public void loadFile2() {
        file2 = chooseFile();
        if (file2 != null) {
            String filePath = file2.getAbsoluteFile().toString();
            labelFile2.setText(filePath);
            textArea2.setText(readFile(file2));
            maskDataFileService.exec(filePath, "textarea2");
            checkFileExists();
            checkAndTriggerNext();
        }

    }


    @FXML
    public void compareFiles() {
        //執行比對並產出檔案
        compareFileExportImpl.run(maskDataFileService.getFileName(), maskDataFileService.getFileData_A(), maskDataFileService.getFileData_B(), getSelectedPrimaryKeyNames(), getSelectedFieldNames());

        showAlert("比對結果檔案 完成");
    }

    private File chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("選擇文字檔案");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));


        // 如果有上次的資料夾，就設定為起始目錄
        if (lastDirectory != null && lastDirectory.exists()) {
            chooser.setInitialDirectory(lastDirectory);
        }

        File selectedFile = chooser.showOpenDialog(null);
        if (selectedFile != null) {
            // 更新上次的資料夾路徑（取得檔案所在資料夾）
            lastDirectory = selectedFile.getParentFile();
        }


        return selectedFile;
    }

    /**
     * 讀檔(BIG5)
     */
    private String readFile(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());

            String content = new String(bytes, Charset.forName(STR_BIG5));
            // 取得第一行（撈取第一筆）
            String firstLine = content.lines().findFirst().orElse("");
            dataLength = firstLine.length();

            return new String(bytes, STR_BIG5);
        } catch (Exception e) {
            return "讀取失敗: " + e.getMessage();
        }
    }

    /**
     * 讀檔(UTF-8)
     */
    private List<String> readLines(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (Exception e) {
            return List.of("讀取失敗: " + e.getMessage());
        }
    }

    private void showAlert(String message) {

//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setHeight(200);
//        alert.setWidth(250);
//        alert.setContentText(message);
//        alert.showAndWait();
        PauseTransition pause = new PauseTransition(Duration.millis(500)); // 延遲 500 毫秒
        pause.setOnFinished(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("訊息");
            alert.setHeaderText(null);
            alert.setContentText(message);
//            alert.showAndWait();
            alert.show();
        });
        pause.play();

    }


    private void columnsCheckBoxList() {

        if (file2 != null && file1 != null) {
            fieldSelectionBox.getChildren().clear();
            pkSelectionBox.getChildren().clear();

            List<String> columns = maskDataFileService.getColumnList();

            if (!columns.isEmpty()) {
                for (String field : columns
                ) {
                    CheckBox checkBox = new CheckBox(field);
                    checkBox.setSelected(true);
                    fieldSelectionBox.getChildren().add(checkBox);
                }

                //確認欄位的CheckBox清單可以顯示後，再去拉PK的CheckBox清單
                pkCheckBoxList(columns);

                btnCompare.setDisable(false);
            } else {
                btnCompare.setDisable(true);
                showAlert("兩邊檔案格式不同，請確認檔案內容");

            }
        }
    }

    private void pkCheckBoxList(List<String> columns) {


        for (String field : columns
        ) {
            CheckBox checkBox = new CheckBox(field);
            if (maskDataFileService.getDataKey().contains(field)) {
                checkBox.setSelected(true);
            } else {
                checkBox.setSelected(false);
            }
            pkSelectionBox.getChildren().add(checkBox);
        }

    }


    /**
     * 取得CheckBox 清單名稱(欄位名稱)
     *
     * @return List<String>
     */
    private List<String> getSelectedFieldNames() {
        return fieldSelectionBox.getChildren().stream()
                .filter(node -> node instanceof CheckBox)
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
    }

    /**
     * 取得CheckBox 清單名稱(主鍵名稱)
     *
     * @return List<String>
     */
    private List<String> getSelectedPrimaryKeyNames() {
        return pkSelectionBox.getChildren().stream()
                .filter(node -> node instanceof CheckBox)
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());
    }

    //計算TEXTAREA寬度
    private double estimateTextWidth(int dataLength, TextArea area) {
        // 粗估單字寬度乘上最長行長度（依據 font size 微調）
        double avgCharWidth = area.getFont().getSize() * 0.6; // 通常等寬字型大約為 0.6 * 字體大小
        return dataLength * avgCharWidth + 50; // 加點 margin
    }


    @FXML
    private void clearScreen() {
        textArea1.clear();
        textArea2.clear();
        labelFile1.setText("未選擇");
        labelFile2.setText("未選擇");
        fieldSelectionBox.getChildren().clear();
        pkSelectionBox.getChildren().clear();
        btnCompare.setDisable(true);
    }
}
