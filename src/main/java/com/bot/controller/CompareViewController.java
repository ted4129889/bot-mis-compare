package com.bot.controller;

import com.bot.dto.CompareSetting;
import com.bot.util.log.LogProcess;
import com.bot.service.mask.DataFileProcessingService;
import com.bot.service.mask.config.FileConfig;
import com.bot.service.mask.config.FileConfigManager;
import com.bot.service.mask.config.SortFieldConfig;
import com.bot.service.output.CompareFileExportImpl;
import com.bot.util.files.TextFileUtil;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

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
    CompareFileExportImpl compareFileExportImpl;
    ToggleGroup toggleGroup = new ToggleGroup();

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
    private RadioButton radioCsv;
    @FXML
    private Label labelFile1;
    @FXML
    private Label labelFile2;
    @FXML
    private CheckBox checkBoxOnlyError;
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

    /**
     * 啟動時 初始化
     */
    @FXML
    public void initialize() {
        //預設按鈕為不啟用
        btnCompare.setDisable(true);
        btnFieldSetting.setDisable(true);

        chooseFileType();

        //刪除清單
        textFileUtil.deleteFile(resultTxt);

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
                System.out.println("打開: " + selected1);
                // 這裡可以呼叫方法開啟檔案內容
            }

            String selected2 = listView2.getSelectionModel().getSelectedItem();
            if (selected2 != null) {
                System.out.println("打開: " + selected2);
                // 這裡可以呼叫方法開啟檔案內容
            }
        });

//        itemRemove.setOnAction(e -> {
//            List<String> selectedItems1 = new ArrayList<>(listView1.getSelectionModel().getSelectedItems());
//            if(selectedItems1 != null){
//                listView1.getItems().removeAll(selectedItems1);
//            }
//
//            List<String> selectedItems2 = new ArrayList<>(listView2.getSelectionModel().getSelectedItems());
//            if(selectedItems2 != null){
//                listView2.getItems().removeAll(selectedItems2);
//            }
//
//        });

//        contextMenu.getItems().addAll(itemOpen, itemRemove);
        contextMenu.getItems().addAll(itemOpen);
        listView1.setContextMenu(contextMenu);
        listView2.setContextMenu(contextMenu);

        //可拖曳資料夾到 ListView
        dragDolder();


    }


    private void chooseFileType() {
//        ToggleGroup exportGroup = new ToggleGroup();
//        radioExcel.setToggleGroup(exportGroup);
//        radioCsv.setToggleGroup(exportGroup);
        radioExcel.setDisable(true);
        radioCsv.setDisable(true);
    }

    /**
     * 將資料夾拖曳到ListView
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
                    Map<String, String> fileMap = Arrays.stream(folder.listFiles())
                            .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".txt"))
                            .collect(Collectors.toMap(
                                    File::getName,
                                    File::getAbsolutePath,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

                    // 顯示檔名在 ListView
                    listView1.getItems().setAll(fileMap.keySet());
                    //顯示路徑
                    labelFile1.setText(folder.getAbsolutePath());

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
                    Map<String, String> fileMap = Arrays.stream(folder.listFiles())
                            .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".txt"))
                            .collect(Collectors.toMap(
                                    File::getName,
                                    File::getAbsolutePath,
                                    (a, b) -> a,
                                    LinkedHashMap::new
                            ));

                    // 顯示檔名在 ListView
                    listView2.getItems().setAll(fileMap.keySet());

                    //顯示路徑
                    labelFile2.setText(folder.getAbsolutePath());

                    newFileNameMap = fileMap;


                    checkFolderExists();


                    // 若你還想保留 Map，建議存進 class 成員變數中
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


//        fieldSelectionBox.getChildren().clear();
        pkSelectionBox.getChildren().clear();
        sortSelectionBox.getChildren().clear();

        maskDataFileService.processPairingColumn(fileName);
//        maskDataFileService.getFieldSetting(fileName);
        List<String> columns = maskDataFileService.getColumnList();


        //先確認檔案名稱是否存在定義檔
        if (maskDataFileService.getXmlAllFileName().contains(fileName)) {
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
            LogProcess.info("fileName===" + fileName);

            FileConfig thisFileConfig = saveFileCongigMap.get(fileName);

            List<String> pkFieldConfigs = thisFileConfig.getPrimaryKeys();
            List<SortFieldConfig> sortFieldConfigs = thisFileConfig.getSortFields();

            //順序預設值 1
            currentOrderIndex = 1;
            for (String field : columns) {

//                genFieldCheckBoxList(field);

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
     * 產生欄位 CheckBox
     */
    private void genFieldCheckBoxList(String field) {
        //欄位的CheckBox清單
        CheckBox checkBoxCol = new CheckBox(field);
        checkBoxCol.setSelected(true);//預設勾選
        checkBoxCol.setDisable(true);//預設不可使用(反白)
        fieldSelectionBox.getChildren().add(checkBoxCol);
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
            orderLabel.setText(String.valueOf(sf.getOrderIndex())); // 顯示從 1 起
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
     * 檢查資料夾都有存在，才會執行
     */
    private void checkFolderExists() {

        if (!oldFileNameMap.isEmpty() && !newFileNameMap.isEmpty()) {
            btnCompare.setDisable(false);
            radioExcel.setDisable(false);
            radioCsv.setDisable(false);
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
            saveFileNameList.add(f + ".txt");
        }
        saveFileCongigMap = FileConfigManager.getConfigMap();
        FileConfigManager.ensureAllFilesExistAndSave(saveFileNameList);

    }


    private void checkFileExists() {
        if (!maskDataFileService.fileExists()) {
            if (!showAlert("檔案名稱不存在定義檔")) return;
        }
    }

    @FXML
    private void loadFolder1() {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("選擇資料夾 1");
        File selectedDir = chooser.showDialog(null);

        // 如果有上次的資料夾，就設定為起始目錄
        if (lastDirectory != null && lastDirectory.exists()) {
            chooser.setInitialDirectory(lastDirectory);
        }
        loading1(selectedDir);
    }

    private void loading1(File selectedDir) {

        if (selectedDir != null) {
            // 更新上次的資料夾路徑（取得檔案所在資料夾）
            lastDirectory = selectedDir;

            labelFile1.setText(selectedDir.getAbsolutePath());


            oldFileNameMap = Arrays.stream(selectedDir.listFiles())
                    .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".txt"))
                    .collect(Collectors.toMap(
                            File::getName,            // key: 檔案名稱
                            File::getAbsolutePath,    // value: 絕對路徑
                            (existing, replacement) -> existing, // 如有重複檔名保留第一個
                            LinkedHashMap::new        // 保持順序
                    ));
//            LogProcess.info("oldFileNameMap = " + oldFileNameMap);

            List<String> fileNameList = Arrays.stream(selectedDir.listFiles())
                    .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".txt"))
//                    .filter(File::isFile)
                    .map(File::getName)
                    .collect(Collectors.toList());

            listView1.getItems().setAll(fileNameList);

            checkFolderExists();
        }
    }

    @FXML
    private void loadFolder2() {
        DirectoryChooser chooser = new DirectoryChooser();

        chooser.setTitle("選擇資料夾 2");
        File selectedDir = chooser.showDialog(null);

        // 如果有上次的資料夾，就設定為起始目錄
        if (lastDirectory != null && lastDirectory.exists()) {
            chooser.setInitialDirectory(lastDirectory);
        }

        loading2(selectedDir);
    }

    private void loading2(File selectedDir) {

        if (selectedDir != null) {
            // 更新上次的資料夾路徑（取得檔案所在資料夾）
            lastDirectory = selectedDir;

            labelFile2.setText(selectedDir.getAbsolutePath());

            newFileNameMap = Arrays.stream(selectedDir.listFiles())
                    .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".txt"))
                    .collect(Collectors.toMap(
                            File::getName,            // key: 檔案名稱
                            File::getAbsolutePath,    // value: 絕對路徑
                            (existing, replacement) -> existing, // 如有重複檔名保留第一個
                            LinkedHashMap::new        // 保持順序
                    ));

//            LogProcess.info("newFileNameMap = " + newFileNameMap);

            List<String> fileNameList = Arrays.stream(selectedDir.listFiles())
                    .filter(file -> file.isFile() && file.getName().toLowerCase().endsWith(".txt"))
//                    .filter(File::isFile)
                    .map(File::getName)
                    .collect(Collectors.toList());

            listView2.getItems().setAll(fileNameList);

            checkFolderExists();


        }
    }


    @FXML
    public void compareFiles() {


        if (radioCsv.isSelected() && radioExcel.isSelected()) {
            compareFileExportImpl.chooseExportFileType = "Both";
        } else if (radioExcel.isSelected()) {
            // 輸出 EXCEL
            compareFileExportImpl.chooseExportFileType = radioExcel.getText();
        } else if (radioCsv.isSelected()) {
            // 輸出 CSV
            compareFileExportImpl.chooseExportFileType = radioCsv.getText();
        } else {
            compareFileExportImpl.chooseExportFileType = "";
            if (!showAlert("請選擇比對輸出的檔案類型")) return;
        }


        boolean outPutOnlyErrorData = checkBoxOnlyError.isSelected();
        //每次比對時，要刪除清單
        textFileUtil.deleteFile(resultTxt);

        CompareSetting setting = CompareSetting.builder()
                .exportOnlyErrorFile(outPutOnlyErrorData).build();

        // 比對後並輸出
        maskDataFileService.exec("", "", oldFileNameMap, newFileNameMap, saveFileCongigMap,setting);

        if (!showAlert("比對完成!")) return;

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


    private boolean showAlert(String message) {

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

//    /**
//     * 確認兩個區域的檔案是否一樣
//     */
//    private void columnsCheckBoxList() {
//
//        if (file2 != null && file1 != null) {
//            fieldSelectionBox.getChildren().clear();
//            pkSelectionBox.getChildren().clear();
//
//            List<String> columns = maskDataFileService.getColumnList();
//
//            if (!columns.isEmpty()) {
//                for (String field : columns
//                ) {
//                    CheckBox checkBox = new CheckBox(field);
//                    checkBox.setSelected(true);
//                    fieldSelectionBox.getChildren().add(checkBox);
//                }
//
//                //確認欄位的CheckBox清單可以顯示後，再去拉PK的CheckBox清單
//                pkCheckBoxList(columns);
//
//                btnCompare.setDisable(false);
//            } else {
//                btnCompare.setDisable(true);
//                if (!showAlert("兩邊檔案格式不同，請確認檔案內容")) return;
//            }
//        }
//    }
//
//    private void pkCheckBoxList(List<String> columns) {
//        for (String field : columns
//        ) {
//            CheckBox checkBox = new CheckBox(field);
//            if (maskDataFileService.getDataKey().contains(field)) {
//                checkBox.setSelected(true);
//            } else {
//                checkBox.setSelected(false);
//            }
//            pkSelectionBox.getChildren().add(checkBox);
//        }
//
//    }


    @FXML
    private void onListView1Click1(MouseEvent event) {
        selectedFileName = listView1.getSelectionModel().getSelectedItem();
        if (selectedFileName != null) {

            ObservableList<String> items = listView2.getItems();
            int index = items.indexOf(selectedFileName);
            if (index != -1) {
                listView2.getSelectionModel().clearAndSelect(index);
                // 若內容多可自動捲動過去
                listView2.scrollTo(index);
                LogProcess.info("selectedItem =" + selectedFileName);
                checkListViewItem(selectedFileName, true);
            } else {
                listView2.getSelectionModel().clearSelection();
                LogProcess.info("222222");
                LogProcess.info("selectedItem =" + selectedFileName);
                checkListViewItem(selectedFileName, false);

            }

        }
    }

    @FXML
    private void onListView1Click2(MouseEvent event) {
        selectedFileName = listView2.getSelectionModel().getSelectedItem();
        if (selectedFileName != null) {
            ObservableList<String> items = listView1.getItems();
            int index = items.indexOf(selectedFileName);
            if (index != -1) {
                listView1.getSelectionModel().clearAndSelect(index);
                listView1.scrollTo(index);
                LogProcess.info("selectedItem =" + selectedFileName);
                checkListViewItem(selectedFileName, true);
            } else {
                listView1.getSelectionModel().clearSelection();
                LogProcess.info("selectedItem =" + selectedFileName);
                checkListViewItem(selectedFileName, false);
            }


        }
    }

    @FXML
    private void saveFields() {

        //暫存設定
        saveConfigFromUI(saveFileName);

        //將暫存設定，存到json檔案
        FileConfigManager.updateOneFile(saveFileCongigMap);

        showAlert("儲存成功");

    }

    private void saveConfigFromUI(String fileName) {

        FileConfig fileConfig = new FileConfig();

        List<String> selectedPKs = new ArrayList<>();
        List<SortFieldConfig> sortOrderList = new ArrayList<>();

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

        fileConfig.setPrimaryKeys(selectedPKs);
        fileConfig.setSortFields(sortOrderList);

        LogProcess.info("fileName.. =" + fileName);
        saveFileCongigMap.put(fileName, fileConfig);


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
        //刪除清單
        textFileUtil.deleteFile(resultTxt);
        checkBoxOnlyError.setSelected(false);
        listView1.getItems().clear();
        listView2.getItems().clear();
        oldFileNameMap = new HashMap<>();
        newFileNameMap = new HashMap<>();
        labelFile1.setText("未選擇");
        labelFile2.setText("未選擇");
//        fieldSelectionBox.getChildren().clear();
        pkSelectionBox.getChildren().clear();
        sortSelectionBox.getChildren().clear();
        btnFieldSetting.setDisable(true);
        btnCompare.setDisable(true);
        radioExcel.setDisable(true);
        radioCsv.setDisable(true);
    }

}
