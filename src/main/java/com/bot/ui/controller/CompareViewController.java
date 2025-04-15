package com.bot.ui.controller;

import com.bot.compare.CompareDataService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@Component
public class CompareViewController {

    @Autowired
    CompareDataService compareDataService;

    @FXML private Button btnLoadFile1;
    @FXML private Button btnLoadFile2;
    @FXML private Label labelFile1;
    @FXML private Label labelFile2;
    @FXML private TextArea textArea1;
    @FXML private TextArea textArea2;

    private File file1;
    private File file2;

    @FXML
    public void loadFile1() {
        file1 = chooseFile();
        if (file1 != null) {
            labelFile1.setText(file1.getName());
            textArea1.setText(readFile(file1));
        }
    }

    @FXML
    public void loadFile2() {
        file2 = chooseFile();
        if (file2 != null) {
            labelFile2.setText(file2.getName());
            textArea2.setText(readFile(file2));
        }
    }

    @FXML
    public void compareFiles() {
        compareDataService.parseData();

//        if (file1 == null || file2 == null) {
//            showAlert("請先選擇兩個檔案！");
//            return;
//        }
//
//        List<String> lines1 = readLines(file1);
//        List<String> lines2 = readLines(file2);
//
//        StringBuilder diff1 = new StringBuilder();
//        StringBuilder diff2 = new StringBuilder();
//
//        int max = Math.max(lines1.size(), lines2.size());
//        for (int i = 0; i < max; i++) {
//            String l1 = i < lines1.size() ? lines1.get(i) : "";
//            String l2 = i < lines2.size() ? lines2.get(i) : "";
//
//            diff1.append(l1);
//            diff2.append(l2);
//
//            if (!l1.equals(l2)) {
//                diff1.append("    <--");
//                diff2.append("    <--");
//            }
//
//            diff1.append("\n");
//            diff2.append("\n");
//        }
//
//        textArea1.setText(diff1.toString());
//        textArea2.setText(diff2.toString());
    }

    private File chooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("選擇文字檔案");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        return chooser.showOpenDialog(null);
    }

    private String readFile(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (Exception e) {
            return "讀取失敗: " + e.getMessage();
        }
    }

    private List<String> readLines(File file) {
        try {
            return Files.readAllLines(file.toPath());
        } catch (Exception e) {
            return List.of("讀取失敗: " + e.getMessage());
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
