package com.bot.ui.controller;

import com.bot.compare.CompareDataService;
import com.bot.compare.CompareDataServiceImpl;
import com.bot.log.LogProcess;
import com.bot.mask.MaskDataFileService;
import com.bot.output.CompareFileExportImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
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

    private File lastDirectory = null;

    @FXML
    public void initialize() {
        //預設按鈕為不啟用
        btnCompare.setDisable(true);

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
                checkAndTriggerNext();

                success = true;
            }
            // 設置拖放完成狀態並消耗事件
            event.setDropCompleted(success);
            event.consume();
        });

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
                checkAndTriggerNext();
                // 可以讀取它的內容或儲存 File 變數
                success = true;
            }

            event.setDropCompleted(success);
            event.consume();
        });


    }

    private void checkAndTriggerNext() {
        String text1 = textArea1.getText();
        String text2 = textArea2.getText();

        if (text1 != null && !text1.isBlank() && text2 != null && !text2.isBlank()) {
            columnsCheckBoxList();
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
        }

        columnsCheckBoxList();
    }


    @FXML
    public void compareFiles() {

        compareFileExportImpl.run(maskDataFileService.getFileData_A(), maskDataFileService.getFileData_B(), getSelectedPrimaryKeyNames(), getSelectedFieldNames());

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
        // 更新上次的資料夾路徑（取得檔案所在資料夾）
        lastDirectory = selectedFile.getParentFile();

        return selectedFile;
    }

    /**
     * 讀檔(BIG5)
     */
    private String readFile(File file) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeight(200);
        alert.setWidth(250);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void columnsCheckBoxList() {

        if (file2 != null && file1 != null) {
            fieldSelectionBox.getChildren().clear();
            pkSelectionBox.getChildren().clear();

            if (!maskDataFileService.getColumnList().isEmpty()) {
                for (String field : maskDataFileService.getColumnList()
                ) {
                    CheckBox checkBox = new CheckBox(field);
                    checkBox.setSelected(true);
                    fieldSelectionBox.getChildren().add(checkBox);
                }

                //確認欄位的CheckBox清單可以顯示後，再去拉PK的CheckBox清單
                pkCheckBoxList();

                btnCompare.setDisable(false);
            } else {
                btnCompare.setDisable(true);
                showAlert("兩邊檔案格式不同，請確認檔案內容");

            }
        }
    }

    private void pkCheckBoxList() {


        for (String field : maskDataFileService.getColumnList()
        ) {
            CheckBox checkBox = new CheckBox(field);
            checkBox.setSelected(false);
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


}
