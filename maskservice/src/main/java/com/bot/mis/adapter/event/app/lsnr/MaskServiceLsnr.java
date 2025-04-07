/* (C) 2025 */
package com.bot.mis.adapter.event.app.lsnr;

import com.bot.mis.adapter.event.app.evt.MaskService;
import com.bot.mis.util.xml.mask.XmlToInsertGenerator;
import com.bot.txcontrol.adapter.event.BatchListenerCase;
import com.bot.txcontrol.config.logger.ApLogHelper;
import com.bot.txcontrol.eum.LogType;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component("MaskServiceLsnr")
@Scope("prototype")
public class MaskServiceLsnr extends BatchListenerCase<MaskService> {

    @Value("${localFile.mis.xml.mask.directory}")
    private String maskXmlFilePath;

    @Autowired XmlToInsertGenerator xmlToInsertGenerator;

    //    @Async("batchThreadPoolTaskExecutor") // 如需平行處理請將此行註解拿掉
    @Override
    public void onApplicationEvent(MaskService event) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "MaskServiceLsnr");
        this.beforRun(event);
    }

    @Override
    @SneakyThrows
    protected void run(MaskService event) {
        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "MaskServiceLsnr run()");

        File folder = new File(maskXmlFilePath);

        List<String> fileNames = listFiles(folder);

        ApLogHelper.info(log, false, LogType.NORMAL.getCode(), "fileNamesList=" + fileNames);

        for (String fileName : fileNames) {
            xmlToInsertGenerator.sqlConvInsertTxt(fileName);
        }
    }

    public List<String> listFiles(File folder) {

        // 取得檔案清單
        return Arrays.stream(folder.listFiles())
                .filter(File::isFile)
                .map(File::getName)
                .collect(Collectors.toList());
    }
}
