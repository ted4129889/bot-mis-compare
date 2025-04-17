package com.bot.mask;


import java.util.List;
import java.util.Map;

public interface MaskDataFileService {


    boolean exec();

    boolean exec(String filePath,String area);

    List<Map<String, String>> getFileData_A();

    List<Map<String, String>> getFileData_B();

    List<String> getDataKey();

    List<String> getColumnList();

}
