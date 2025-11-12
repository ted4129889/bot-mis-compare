package com.bot.dataprocess;


import com.bot.dto.CompareSetting;
import com.bot.service.compare.CompareDataService;
import com.bot.service.mask.config.FileConfig;
import com.bot.service.mask.config.SortFieldConfig;
import com.bot.service.output.CompareFileExportImpl;
import com.bot.service.output.templates.CompareResultRpt;
import com.bot.util.files.FileNameUtil;
import com.bot.util.files.TextFileUtil;
import com.bot.util.log.LogProcess;
import com.bot.util.mask.MaskUtil;
import com.bot.util.parse.Parse;
import com.bot.util.xml.mask.DataMasker;
import com.bot.util.xml.mask.XmlParser;
import com.bot.util.xml.vo.XmlData;
import com.bot.util.xml.vo.XmlField;
import com.bot.util.xml.vo.XmlFile;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface Job {
    /**
     * 執行入口方法
     */
    void execute();
}
