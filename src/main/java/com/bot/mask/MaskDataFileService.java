package com.bot.mask;


import com.bot.mask.config.FileConfig;
import com.bot.util.xml.vo.XmlData;

import java.util.List;
import java.util.Map;

public interface MaskDataFileService {

    /**
     * 執行檔案遮罩(遮蔽使用)
     * */
    boolean exec();

    /**
     * 執行檔案處理(比對使用)
     * */
    boolean exec(String filePath,String area);

    /**
     * 執行檔案處理(比對使用)
     * */
    boolean exec(String filePath, String area, Map<String, String> oldFileNameList, Map<String, String> newFileNameList, Map<String, FileConfig> fieldSettingList);
    /**
     * 檔案是否存在
     * */
    boolean fileExists();
    /**
     * 取得TextArea1元件中的選擇的檔案內容
     * */
    List<Map<String, String>> getFileData_A();
    /**
     * 取得TextArea2元件中的選擇的檔案內容
     * */
    List<Map<String, String>> getFileData_B();

    /**
     * 取得檔案的key欄位
     * */
    List<String> getDataKey();
    /**
     * 取得檔案的key欄位
     * */
//    void setDataKey(List<String> dataKeyList){};
    /**
     * 取得檔案的所有欄位名稱
     * */
    List<String> getColumnList();
    /**
     * 取得定義檔(Xml)中的所有檔案名稱
     * */
    List<String> getXmlAllFileName();

   String getFileName();

   void processPairingColumn( String fileName);
    Map<String, FileConfig> getFieldSetting();



}
