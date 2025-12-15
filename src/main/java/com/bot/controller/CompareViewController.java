package com.bot.controller;

import com.bot.config.CompareSetting;
import com.bot.mask.DataFileProcessingService;
import com.bot.mask.DataFileProcessingServiceImpl;
import com.bot.mask.config.FileConfig;
import com.bot.mask.config.FileConfigManager;
import com.bot.mask.config.SortFieldConfig;
import com.bot.output.CompareFileExportImpl;
import com.bot.util.files.TextFileUtil;
import com.bot.util.log.LogProcess;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CompareViewController {
    //    @Value("${localFile.mis.batch.compare.new.directory}")
//    private String newFilePath;
//    @Value("${localFile.mis.batch.compare.old.directory}")
//    private String oldFilePath;
    @Value("${localFile.mis.compare_result.txt}")
    private String resultTxt;
    private String STR_BIG5 = "Big5";
    private String STR_UTF8 = "UTF-8";


    @Autowired
    TextFileUtil textFileUtil;
    @Autowired
    DataFileProcessingService maskDataFileService;

    @Autowired
    DataFileProcessingServiceImpl dataFileProcessingServiceImpl;
    @Autowired
    CompareFileExportImpl compareFileExportImpl;
    private Path lastDirectoryPath; // 紀錄上一次選擇的資料夾
    @FXML
    public Button btnClear;
    @FXML
    private Button btnLoadFile1;
    @FXML
    private Button btnLoadFile2;
    @FXML
    private Button btnCompare;
    @FXML
    private Button btnFieldSetting;
    @FXML
    private RadioButton radioExcel;
    @FXML
    private RadioButton radioTxt;
    @FXML
    private RadioButton radioCsv;
    @FXML
    private Label labelFile1;
    @FXML
    private Label labelFile2;
    @FXML
    private Label hintLabel1;
    @FXML
    private Label hintLabel2;
    @FXML
    public Label listViewlabel1;
    @FXML
    public Label listViewlabel2;
    @FXML
    private CheckBox checkBoxOnlyError;
    //    @FXML
//    private CheckBox checkBoxUseMask;
    @FXML
    private VBox fieldSelectionBox;
    @FXML
    private VBox pkSelectionBox;
    @FXML
    private VBox sortSelectionBox;
    private File file1;
    private File file2;
    private File lastDirectory = null;
    @FXML
    private ListView<String> listView1;
    @FXML
    private ListView<String> listView2;
    Map<String, String> oldFileNameMap = new HashMap<>();
    Map<String, String> newFileNameMap = new HashMap<>();

    private String selectedFileName = "";
    private String lastSelectFileName = "";
    private String saveFileName = "";
    private List<String> saveFileNameList = new ArrayList<>();

    private Map<String, FileConfig> saveFileCongigMap = new LinkedHashMap<>();
    private FileConfig saveFileConfig = new FileConfig();
    private String fontStyle = "-fx-font-size: 12px; -fx-font-family: \"Microsoft JhengHei\", \"Arial\", sans-serif;";
    int dataLength = 0;
    int currentOrderIndex = 1;

    private String oldPath = "";
    private String newPath = "";


    /**
     * 啟動時 初始化
     */
    @FXML
    public void initialize() {
        init();

        // 設定多選模式(FXML上也要多)
//        listView1.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE)。;
//        listView2.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // 設定右鍵選單
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemOpen = new MenuItem("打開");
//        MenuItem itemRemove = new MenuItem("移除");

        itemOpen.setOnAction(e -> {
            String selected1 = listView1.getSelectionModel().getSelectedItem();
            if (selected1 != null) {
                String s1 = oldPath + "\\" + selected1;
                LogProcess.info(log, "打開: " + s1);
                openFileSmart(new File(s1));
            }

            String selected2 = listView2.getSelectionModel().getSelectedItem();
            if (selected2 != null) {
                String s2 = newPath + "\\" + selected2;
                System.out.println("打開: " + s2);
                openFileSmart(new File(s2));
            }
        });

        contextMenu.getItems().addAll(itemOpen);
        listView1.setContextMenu(contextMenu);
        listView2.setContextMenu(contextMenu);

        //可拖曳資料夾到 ListView
        dragDolder();


    }

    /**
     * 相關元件初始化
     */
    private void init() {
        //預設按鈕為不啟用
        btnCompare.setDisable(true);
        btnFieldSetting.setDisable(true);
        btnClear.setDisable(true);
        checkBoxOnlyError.setDisable(true);
        checkBoxOnlyError.setSelected(false);

//        checkBoxUseMask.setDisable(true);
//        checkBoxUseMask.setSelected(false);


        //左側提示標籤 不啟用
        hintLabel1.setVisible(false);
        hintLabel2.setVisible(false);

        //清單提示標籤 啟用
        listViewlabel1.setVisible(true);
        listViewlabel2.setVisible(true);


        //匯出檔案類型不能點選
        //        ToggleGroup exportGroup = new ToggleGroup();
//        radioExcel.setToggleGroup(exportGroup);
//        radioCsv.setToggleGroup(exportGroup);
        radioExcel.setDisable(true);
        radioCsv.setDisable(true);


        //刪除清單
        textFileUtil.deleteFile(resultTxt);


    }

    /**
     * 將資料夾拖曳到 ListView 功能
     */
    private void dragDolder() {

        listView1.setOnDragOver(event -> {
            if (event.getGestureSource() != listView1 &&
                    event.getDragboard().hasFiles() &&
                    event.getDragboard().getFiles().stream().anyMatch(File::isDirectory)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        listView1.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File folder = db.getFiles().get(0); // 只處理第一個拖入的資料夾
                if (folder.isDirectory()) {
                    Map<String, String> fileMap = Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                            .filter(File::isFile)
                            .collect(Collectors.toMap(
                                    File::getName,
                                    File::getAbsolutePath,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

                    // 顯示檔名在 ListView
                    listView1.getItems().setAll(fileMap.keySet());
                    //顯示路徑
                    oldPath = folder.getAbsolutePath();
                    labelFile1.setText(oldPath);

                    oldFileNameMap = fileMap;

                    checkFolderExists();


                    // 若你還想保留 Map，建議存進 class 成員變數中

                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });


        listView2.setOnDragOver(event -> {
            if (event.getGestureSource() != listView2 &&
                    event.getDragboard().hasFiles() &&
                    event.getDragboard().getFiles().stream().anyMatch(File::isDirectory)) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        listView2.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                File folder = db.getFiles().get(0); // 只處理第一個拖入的資料夾
                if (folder.isDirectory()) {
                    Map<String, String> fileMap = Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                            .filter(File::isFile)
                            .collect(Collectors.toMap(
                                    File::getName,
                                    File::getAbsolutePath,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

                    // 顯示檔名在 ListView
                    listView2.getItems().setAll(fileMap.keySet());

                    newPath = folder.getAbsolutePath();
                    //顯示路徑
                    labelFile2.setText(newPath);
                    labelFile2.setText(newPath);

                    newFileNameMap = fileMap;

                    checkFolderExists();

                    // this.fileMap1 = fileMap;


                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });
    }


    /**
     * 檢查ListView1 和 檢查ListView2 選許的項目決定是否顯示欄位跟PK
     */
    private void checkListViewItem(String fileName, boolean bothExist) {
//        LogProcess.info(log, "fileName111 = " + fileName);
        int idx = textFileUtil.replaceDateWithPlaceholder(fileName).lastIndexOf("_");
        fileName = (idx > 0) ? fileName.substring(0, idx) : fileName;

//        fileName = textFileUtil.replaceDateWithPlaceholder(fileName);

        pkSelectionBox.getChildren().clear();
        sortSelectionBox.getChildren().clear();

        maskDataFileService.processPairingColumn(fileName);
        LogProcess.info(log, "fileName = " + fileName);
//        List<String> columns = maskDataFileService.getColumnList();
        List<String> columns = maskDataFileService.getColumnList();


        LogProcess.info(log, "maskDataFileService.getXmlAllFileName() = " + maskDataFileService.getXmlAllFileName());

        //先確認檔案名稱是否存在定義檔
        if (!maskDataFileService.getXmlAllFileName().contains(fileName)) {
            if (!showAlert("檔案名稱不存在定義檔")) {
                btnFieldSetting.setDisable(true);
                btnCompare.setDisable(true);
                radioExcel.setDisable(true);
                radioCsv.setDisable(true);
                return;
            }
        }

        //先確認兩個檔案名稱是否都有
        if (!bothExist) {
            if (!showAlert("新舊檔案有缺少檔案")) {
                btnFieldSetting.setDisable(true);
                btnCompare.setDisable(true);
                radioExcel.setDisable(true);
                radioCsv.setDisable(true);
                return;
            }
        }

        if (!columns.isEmpty()) {
            //每選擇檔案名稱都要先記錄存檔的
            saveFileName = fileName;

            FileConfig thisFileConfig = saveFileCongigMap.get(fileName);

            List<String> pkFieldConfigs = thisFileConfig.getPrimaryKeys();
            List<SortFieldConfig> sortFieldConfigs = thisFileConfig.getSortFields();

            //順序預設值 1
            currentOrderIndex = 1;
            for (String field : columns) {

                genPkColumnCheckBoxList(field, pkFieldConfigs);

                genSortColumnRadioList(field, sortFieldConfigs);

            }

            btnFieldSetting.setDisable(false);
            btnCompare.setDisable(false);
            radioExcel.setDisable(false);
            radioCsv.setDisable(false);
        } else {
            btnFieldSetting.setDisable(true);
            btnCompare.setDisable(true);
            radioExcel.setDisable(true);
            radioCsv.setDisable(true);
            if (!showAlert("檔案名稱不存在定義檔")) return;
        }


    }

    /**
     * 產生PK欄位 CheckBox
     */
    private void genPkColumnCheckBoxList(String field, List<String> selectedKeys) {
        //PK的CheckBox清單
        Label label = new Label(field);
        label.setPrefWidth(120);
        label.setStyle(fontStyle);
        CheckBox checkBoxPK = new CheckBox();

        checkBoxPK.setSelected(selectedKeys.contains(field));

        HBox row = new HBox(10, label, checkBoxPK);
        pkSelectionBox.getChildren().add(row);


    }


    /**
     * 產生排序欄位 Radio
     */
    private void genSortColumnRadioList(String field, List<SortFieldConfig> sortFieldConfigs) {
        //Sort欄位
        Label label = new Label(field);
        label.setPrefWidth(100);
        label.setStyle(fontStyle);
        RadioButton asc = new RadioButton("ASC");
        RadioButton desc = new RadioButton("DESC");


        ToggleGroup sortGroup = new ToggleGroup();
        asc.setStyle(fontStyle);
        asc.setToggleGroup(sortGroup);
        desc.setStyle(fontStyle);
        desc.setToggleGroup(sortGroup);
        Label orderLabel = new Label(""); // 顯示排序順序
        orderLabel.setPrefWidth(40);
        orderLabel.setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");

        Optional<SortFieldConfig> optionalConfig = sortFieldConfigs.stream()
                .filter(s -> s.getFieldName().equals(field))
                .findFirst();


        if (optionalConfig.isPresent()) {
            SortFieldConfig sf = optionalConfig.get();
            orderLabel.setText(String.valueOf(sf.getOrderIndex()));
            //如果順序大於預設值1 那就覆蓋預設值
            if (sf.getOrderIndex() >= currentOrderIndex) {
                currentOrderIndex = sf.getOrderIndex() + 1;
            }

            if (sf.isAscending()) {
                asc.setSelected(true);
            } else {
                desc.setSelected(true);
            }
        }

        // 選擇 ASC
        asc.setOnAction(e -> {
            if (asc.isSelected() && orderLabel.getText().isEmpty()) {
                orderLabel.setText(String.valueOf(currentOrderIndex++));
            }
        });

        // 選擇 DESC
        desc.setOnAction(e -> {
            if (desc.isSelected() && orderLabel.getText().isEmpty()) {
                orderLabel.setText(String.valueOf(currentOrderIndex++));
            }
        });

        asc.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (asc.isSelected()) {
                String currentOrderStr = orderLabel.getText();
                if (!currentOrderStr.isEmpty()) {
                    int removedOrder = Integer.parseInt(currentOrderStr);
                    adjustOrderAfterRemove(removedOrder);
                }

                sortGroup.selectToggle(null);
                orderLabel.setText("");
                currentOrderIndex--;
                event.consume();
            }
        });

        desc.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            if (desc.isSelected()) {
                String currentOrderStr = orderLabel.getText();
                if (!currentOrderStr.isEmpty()) {
                    int removedOrder = Integer.parseInt(currentOrderStr);
                    adjustOrderAfterRemove(removedOrder);
                }

                sortGroup.selectToggle(null);
                orderLabel.setText("");
                currentOrderIndex--;
                event.consume();
            }

        });

        HBox row = new HBox(10, label, asc, desc, new Label("順序:"), orderLabel);
        sortSelectionBox.getChildren().add(row);
    }

    /**
     * 欄位排序順序計算功能
     */
    private void adjustOrderAfterRemove(int removedOrder) {
        ObservableList<Node> rows = sortSelectionBox.getChildren();
        for (Node rowNode : rows) {
            if (rowNode instanceof HBox row) {
                List<Node> children = row.getChildren();
                if (children.size() >= 5) {
                    Label orderLabel = (Label) children.get(4);
                    String text = orderLabel.getText();
                    if (!text.isEmpty()) {
                        int currentOrder = Integer.parseInt(text);
                        if (currentOrder > removedOrder) {
                            orderLabel.setText(String.valueOf(currentOrder - 1));
                        }
                    }
                }
            }
        }
    }

    /**
     * 檢查 兩邊的清單 是否存在，決定元件是否啟用
     */
    private void checkFolderExists() {
        //確認 清單 皆有匯入
        if (!oldFileNameMap.isEmpty() && !newFileNameMap.isEmpty()) {
            //啟用按鈕
            btnCompare.setDisable(false);
            btnClear.setDisable(false);
            radioExcel.setDisable(false);
            radioCsv.setDisable(false);
            checkBoxOnlyError.setDisable(false);
//            checkBoxUseMask.setDisable(false);

            //顯示提示
            hintLabel1.setVisible(true);
            hintLabel2.setVisible(true);

            //都有在才會執行讀取設定檔案
            loadFieldSetting();
        }
    }

    /**
     * 載入檔案欄位設定(json)
     */
    private void loadFieldSetting() {
        // 你目前載入的檔案名稱
        saveFileNameList = new ArrayList<>();
        for (String f : maskDataFileService.getXmlAllFileName()) {
            saveFileNameList.add(f);
        }
        saveFileCongigMap = FileConfigManager.getConfigMap();
        FileConfigManager.ensureAllFilesExistAndSave(saveFileNameList);

    }

    /**
     * 原始檔案資料夾位置(按鈕功能)
     */
    @FXML
    private void loadFolder1() {
        loadFolder("選擇資料夾 1", labelFile1, listView1, true);
    }

    /**
     * 需比對檔案資料夾位置(按鈕功能)
     */
    @FXML
    private void loadFolder2() {
        loadFolder("選擇資料夾 2", labelFile2, listView2, false);
    }

    /**
     * 選擇 檔案資料夾位置 共用功能
     */
    private void loadFolder(String title, Label label, ListView<String> listView, boolean isOld) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(title);

        // 設定初始資料夾（如果有）
        if (lastDirectoryPath != null) {
            File initialDir = lastDirectoryPath.toFile();
            if (initialDir.exists() && initialDir.isDirectory()) {
                chooser.setInitialDirectory(initialDir);
            }
        }

        File selectedDir = chooser.showDialog(null);
        if (selectedDir == null) return;

        // 記錄最後的路徑
        lastDirectoryPath = selectedDir.toPath();

        // 顯示於 Label
        label.setText(selectedDir.getAbsolutePath());

        File[] txtFiles = selectedDir.listFiles(File::isFile);
        if (txtFiles == null) return;

        Map<String, String> fileMap = Arrays.stream(txtFiles)
                .collect(Collectors.toMap(
                        File::getName,
                        File::getAbsolutePath,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        List<String> fileNames = Arrays.stream(txtFiles)
                .map(File::getName)
                .collect(Collectors.toList());

        listView.getItems().setAll(fileNames);

        if (isOld) {
            oldFileNameMap = fileMap;
        } else {
            newFileNameMap = fileMap;
        }

        checkFolderExists();
    }

    /**
     * 比對按鈕
     */
    @FXML
    public void compareFiles() {

        if (radioTxt.isSelected()) {
//            compareFileExportImpl.chooseExportFileType = "Both";
//            dataFileProcessingServiceImpl.chooseExportFileType = "Both";
            compareFileExportImpl.chooseExportFileType = radioTxt.getText();
            dataFileProcessingServiceImpl.chooseExportFileType = radioTxt.getText();
        } else if (radioExcel.isSelected()) {
            // 輸出 EXCEL
            compareFileExportImpl.chooseExportFileType = radioExcel.getText();
            dataFileProcessingServiceImpl.chooseExportFileType = radioExcel.getText();
        } else if (radioCsv.isSelected()) {
            // 輸出 CSV
            compareFileExportImpl.chooseExportFileType = radioCsv.getText();
            dataFileProcessingServiceImpl.chooseExportFileType = radioCsv.getText();
        } else {
            compareFileExportImpl.chooseExportFileType = "";
            dataFileProcessingServiceImpl.chooseExportFileType = "";
            if (!showAlert("請選擇比對輸出的檔案類型")) return;
        }


        boolean outPutOnlyErrorData = checkBoxOnlyError.isSelected();

        boolean outPutUseMask = false;
//        boolean outPutUseMask = checkBoxUseMask.isSelected();
        //每次比對時，要刪除清單
        textFileUtil.deleteFile(resultTxt);

        CompareSetting setting = CompareSetting.builder()
                .exportOnlyErrorFile(outPutOnlyErrorData).exportUseMask(outPutUseMask).build();

        LocalDateTime dateTime = LocalDateTime.now();
        compareFileExportImpl.dateTime = dateTime;
        // 比對後並輸出
        maskDataFileService.exec(oldFileNameMap, newFileNameMap, saveFileCongigMap, setting);

        if (!showAlert("比對完成!")) return;

    }


    /**
     * 顯示提示訊息
     */
    public boolean showAlert(String message) {

//        Alert alert = new Alert(Alert.AlertType.INFORMATION);
//        alert.setHeight(200);
//        alert.setWidth(250);
//        alert.setContentText(message);
//        alert.showAndWait();
        PauseTransition pause = new PauseTransition(Duration.millis(100)); // 延遲 500 毫秒
        pause.setOnFinished(event -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("訊息");
            alert.setHeaderText(null);
            alert.setContentText(message);
//            alert.showAndWait();
            alert.show();
        });
        pause.play();

        return false;

    }

    /**
     * 原始檔案清單
     */
    @FXML
    private void onListView1Click1(MouseEvent event) {
        syncSelection(listView1, listView2, true);
    }

    /**
     * 比對檔案清單
     */
    @FXML
    private void onListView1Click2(MouseEvent event) {
        syncSelection(listView2, listView1, false);
    }

    /**
     * 清單共用功能
     */
    private void syncSelection(ListView<String> sourceListView,
                               ListView<String> targetListView,
                               boolean isSourceOldToNew) {
        if (oldFileNameMap.isEmpty() || newFileNameMap.isEmpty()) return;

        String selected = sourceListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        ObservableList<String> targetItems = targetListView.getItems();
        int index = targetItems.indexOf(selected);

        if (index != -1) {
            targetListView.getSelectionModel().clearAndSelect(index);
            targetListView.scrollTo(index);
            checkListViewItem(selected, true);
        } else {
            targetListView.getSelectionModel().clearSelection();
            checkListViewItem(selected, false);
        }

        selectedFileName = selected; // 如果你仍需要這個變數
    }


    /**
     * 儲存UI設定(按鈕)
     */
    @FXML
    private void saveFields() {
        //暫存設定
        saveConfigFromUI(saveFileName);

        //將暫存設定，存到json檔案
        FileConfigManager.updateOneFile(saveFileCongigMap);

        showAlert("儲存成功");
        //儲存後，重新reload
        loadFieldSetting();

    }

    /**
     * 儲存 UI設定
     */
    private void saveConfigFromUI(String fileName) {

        FileConfig fileConfig = new FileConfig();

        fileConfig.setPrimaryKeys(pkFieldSetting());
        fileConfig.setSortFields(sortFieldSetting());

//        LogProcess.info("fileName.. =" + fileName);
        saveFileCongigMap.put(fileName, fileConfig);
    }

    /**
     * 主鍵欄位設定
     */
    private List<String> pkFieldSetting() {

        List<String> selectedPKs = new ArrayList<>();

        //pk欄位
        for (Node node : pkSelectionBox.getChildren()) {
            if (node instanceof HBox hBox) {
                Label fieldLabel = (Label) hBox.getChildren().get(0);
                CheckBox pk = (CheckBox) hBox.getChildren().get(1);

                String fieldName = fieldLabel.getText();
                if (pk.isSelected()) {
                    selectedPKs.add(fieldName);
                }

            }
        }
        return selectedPKs;
    }

    /**
     * 欄位排序設定
     */
    private List<SortFieldConfig> sortFieldSetting() {

        List<SortFieldConfig> sortOrderList = new ArrayList<>();
        //sort欄位
        for (Node node : sortSelectionBox.getChildren()) {
            if (node instanceof HBox hBox) {

                Label sortIndex = (Label) hBox.getChildren().get(4);

                String s = sortIndex.getText();

                if (!s.isEmpty()) {
                    int index = sortIndex.getText().isEmpty() ? 0 : Integer.parseInt(sortIndex.getText());

                    Label fieldLabel = (Label) hBox.getChildren().get(0);
                    String fieldName = fieldLabel.getText();

                    RadioButton asc = (RadioButton) hBox.getChildren().get(1);
                    RadioButton desc = (RadioButton) hBox.getChildren().get(2);


                    boolean sortFlag = false;
                    if (asc.isSelected()) {
                        sortFlag = true;
                    } else if (desc.isSelected()) {
                        sortFlag = false;
                    }


                    sortOrderList.add(new SortFieldConfig(fieldName, sortFlag, index));
                }
            }
        }
        return sortOrderList;
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
     * 取得HBox中的CheckBox 清單名稱(主鍵名稱)
     *
     * @return List<String>
     */
    private List<String> getSelectedPrimaryKeyNames() {
        return pkSelectionBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .map(hbox -> {
                    Label label = (Label) hbox.getChildren().get(0);
                    CheckBox checkBox = (CheckBox) hbox.getChildren().get(1);
                    return checkBox.isSelected() ? label.getText() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 取得HBox中的 Label 跟 RadioButton
     *
     * @return Map<String, Boolean>
     */
    private Map<String, Boolean> getSelectedSortNames() {
        return sortSelectionBox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .map(node -> (HBox) node)
                .map(hbox -> {
                    Label label = (Label) hbox.getChildren().get(0);
                    RadioButton asc = (RadioButton) hbox.getChildren().get(1);
                    RadioButton desc = (RadioButton) hbox.getChildren().get(2);

                    String field = label.getText();
                    if (asc.isSelected()) {
                        return Map.entry(field, true); // ASC
                    } else if (desc.isSelected()) {
                        return Map.entry(field, false); // DESC
                    } else {
                        return null; // 沒有選擇排序方向就跳過
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }


    @FXML
    private void clearScreen() {

        init();

        //左側欄位列表清除
        pkSelectionBox.getChildren().clear();
        sortSelectionBox.getChildren().clear();

        //路徑標籤還原
        oldFileNameMap = new HashMap<>();
        newFileNameMap = new HashMap<>();
        labelFile1.setText("未選擇");
        labelFile2.setText("未選擇");

        //清單內容清空
        listView1.getItems().clear();
        listView1.setVisible(true);
        listView2.getItems().clear();
        listView2.setVisible(true);


    }

//    private void openFile(File file) {
////        String currentDir = Paths.get("").toAbsolutePath().toString();
////        LogProcess.info("目前專案路徑1：" + currentDir);
////        LogProcess.info("file path：" + file);
////        String currentDir = System.getProperty("user.dir");
////
//        LogProcess.info("file.exists()：" + file.exists());
//        LogProcess.info("Desktop.isDesktopSupported()：" + Desktop.isDesktopSupported());

    /// /        File nFile = new File(currentDir);
//        try {
//            if (file.exists() && Desktop.isDesktopSupported()) {
//                Desktop.getDesktop().open(file);
//            } else {
//                LogProcess.info("找不到檔案或不支援 Desktop 開啟: " + file.getAbsolutePath());
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    private void openFileSmart(File file) {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
                return;
            } catch (Exception e) {
                System.out.println("使用 Desktop 開啟失敗，改用命令行開啟");
            }
        }
        openFileCrossPlatform(file);
    }

    private void openFileCrossPlatform(File file) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("cmd", "/c", "start", "", file.getAbsolutePath()).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            } else if (os.contains("nix") || os.contains("nux")) {
                new ProcessBuilder("xdg-open", file.getAbsolutePath()).start();
            } else {
                System.out.println("不支援的作業系統");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
