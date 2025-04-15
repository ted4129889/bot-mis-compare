package com.bot.compare;


import com.bot.util.comparator.DataComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CompareDataServiceImpl implements CompareDataService{
    @Value("${localFile.mis.batch.compare.new.directory}")
    private String newFilePath;
    @Value("${localFile.mis.batch.compare.old.directory}")
    private String oldFilePath;
    @Value("${localFile.mis.xml.mask.directory}")
    private String maskXmlFilePath;
    @Value("${localFile.mis.xml.output.directory}")
    private String outputPath;
    @Autowired
    private DataComparator dataComparator;
    @Override
    public void parseData() {
        try {
            dataComparator.executeCompare(newFilePath, oldFilePath, maskXmlFilePath, outputPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
