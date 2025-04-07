### Version:20250203
# Main Project
### com-bot-tools
# Env Setting
### Install JDK 21
### Install IntelliJ lombok Plugins
### LOG 位置：LOG/MIS/
- #### log檔案名稱輸出格式： %d{yyyy-MM-dd}-${userId}-%i.log
### external-parameters.properties：外部參數檔(ex:設定資料庫連線參數)
### external-config 定義檔文件
- #### external-config/xml/input 讀取檔案使用之定義檔(for 程式使用)
- #### external-config/xml/output 輸出檔案使用之定義檔(for 程式使用)
- #### external-config/xml/mask 各個DB Table使用的遮罩定義檔(for 工具使用)
- #### external-config/xml/mask/convert 遮罩之定義檔(for 工具使用)
### batch-file 檔案放置文件
- #### batch-file/input 程式讀取檔案之位置
- #### batch-file/output 程式輸出檔案之位置
- #### batch-file/bots_input 程式讀取主機相關檔案之位置
- #### batch-file/bots_output 程式輸出主機相關檔案之位置
## 子專案分類
- ### Api
- ### Service
## 子專案結構
- ### Api
    - #### Input Value Object
        - #### com.bot.mis/adapter/in/api/
    - #### Output Value Object
        - #### com.bot.mis/adapter/out/api
    - #### Value Object Mapper
        - #### com.bot.mis/mapper/
- ### Service
    - #### 交易事件 (可透過工具產生空白範例)
        - #### com.bot.mis/adapter/event/app/evt/
    - #### 交易監聽器(交易邏輯撰寫位置),(可透過工具產生空白範例)
        - #### com.bot.mis/adapter/event/app/lsnr/
    - #### Service Input Value Object(可透過工具讀取xml產生)
        - #### com.bot.mis/adapter/in/svc/
    - #### Service Output Value Object(可透過工具讀取xml產生)
        - #### com.bot.mis/adapter/out/svc/
- ### Util
    - #### VO && class
        - #### com/bot/(專案名稱)/util/(分類1)/(小分類2)/.../
## 撰寫流程說明
- ### 設定參數
    - ### application.yml
          spring.profiles.active 環境設定
    - ### application-(local, dev, uat, sit).yml
          spring.datasource DB相關
          grpc.server.port gRpc 對外Port
          grpc.server.address gRpc 對外ip
          grpc.client.port gRpc 連外Port
          grpc.client.address gRpc 連外ip
          astar.binPath 滿天星Table位置(astar)
    - ### MgGlobal.yml
          MG相關參數預設 (本機測試使用)

- ### 撰寫流程Api設定檔
    - #### apili.name
        - #### Api Name
    - #### apili.fmtId
        - #### CL_BATCH_I
    - #### apili.txType
        - #### I 查詢類 U 更新類
    - #### apili.exProc
        - #### 當整個交易Api流程中有錯誤，將會rollback 後執行此Service (如有Service設定commit 將會先沖正後執行此Service)
    - #### apili.service.name
        - #### 服務名稱
    - #### apili.service.commit
        - #### 服務結束時，是否先行Commit; true is commit first
    - #### apili.service.exProc
        - #### service 錯誤時需不踢錯誤，不rollback，執行此設定Service 詳細請看流程「SAPI01」<br>
    - #### apili.service.funcname
        - #### Input Value Object(以下簡稱IV)須透過先前服務的Output Value Object(以下簡稱OV)賦值的Mapper method名稱
    - #### apili.service.resMapping.serviceName
        - #### 需取用OV的服務名稱
    - #### apili.service.resMapping.msgCode
        - #### 需取用OV的MSGID(同一服務OV會有多筆不同MSGID返回)
        - 
## TxControl Util
- ### ApplicationContext
    - #### ApplicationContextUtil
          ex. ApplicationContextUtil.getBean("text2VoFormatter", Text2VoFormatter.class);
- ### Text 2 Vo
    - #### Text2VoFormatter (使用@Autowired or ApplicationContextUtil.getText2VoFormatter())
          ex. @Autowired Text2VoFormatter text2VoFormatter;
              text2VoFormatter.format(data, request);
              data : byte
              request : value object 
- ### Vo 2 Text
    - #### Vo2TextFormatter (使用@Autowired or ApplicationContextUtil.getVo2TextFormatter())
           ex. @Autowired Vo2TextFormatter vo2TextFormatter;
               String utf8String = vo2TextFormatter.formatRS(responseCase);
               byte[] burBytes = vo2TextFormatter.formatRB(responseCase);
               responseCase : response value object
- ### 轉型
    - #### Parse(使用@Autowired or ApplicationContextUtil.getParse())
        - #### method
            - #### decimal2String(T value)
                  將數字型別(int long float double BigDecimal)轉型為字串，如轉型失敗，回傳值為NULL
            - #### decimal2StringPadZero(T value, int precision)
                  將數字型別(int long float double BigDecimal)轉型為字串並且前補0，如轉型失敗，回傳值為NULL
            - #### string2BigDecimal(String value)
                  將字串轉型為igDecimal，如轉型失敗，回傳值為NULL
            - #### string2Long(String value)
                  將字串轉型為Long，如轉型失敗，回傳值為NULL
            - #### string2Integer(String value)
                  將字串轉型為Integer，如轉型失敗，回傳值為NULL
            - #### string2Short(String value)
                  將字串轉型為Short，如轉型失敗，回傳值為NULL
            - #### isNumeric(String value)
                  判斷字串是否為數字
- ### 補位
    - #### FormatUtil
        - #### method
            - #### FormatUtil.padX(String value, int len)
                  字串右補空白
            - #### FormatUtil.padLeft(String value, int len)
                  字串左補空白
            - #### FormatUtil.pad9(String value, int len)
                  左補0
            - #### FormatUtil.rightPad9(String value, int len)
                  右補0
            - #### FormatUtil.right(String value, int len)
                  留右邊幾位
            - #### FormatUtil.left(String value, int len)
                  留左邊幾位
            - #### FormatUtil.pad9(String n, int width, int afterDecimalPoint)
                  左補0含小數位數字
            - #### FormatUtil.vo2JsonString(T sourceObj)
                  將物件轉為json字串
            - #### FormatUtil.jsonString2Vo(String text, Class<T> sourceVo)
                  將字串轉為對應物件
- ### 字串切位
    - #### CutAndCount
        - #### method
            - #### CutAndCount.stringCutBaseOnBUR(String text, int sPos, int len)
                  以優利碼長度計算切位
            - #### CutAndCount.stringCutBaseOnBig5(String text, int sPos, int len)
                  以BIG5長度計算切位
            - #### CutAndCount.stringCutFRB(String text, String code, int byteLen)
                  以自訂編碼切位(從0開始算)
- ### Date
    - #### DateUtil(@Autowired or ApplicationContextUtil.getDateUtil())
        - #### method
            - #### boolean dateChk(String value | int value)
                  檢查日期是否正確
            - #### int bcToRoc(int value)
                  西元年轉民國年 (轉換失敗 return 0)
            - #### int rocToBc(int value)
                  民國年轉西元年 (轉換失敗 return 0)
            - #### int getNowIntegerRoc()
                  取得系統民國日期整數
            - #### String getNowStringRoc()
                  取得系統民國日期字串
            - #### int getNowIntegerForBC()
                  取得系統西元日期整數
            - #### String getNowStringBc()
                  取得系統西元日期字串
            - #### int getNowIntegerTime(boolean isHaveFemto)
                  取得系統時間整數 true HHmmssSSS false HHmmss
            - #### String getNowStringTime(boolean isHaveFemto)
                  取得系統時間字串 true HHmmssSSS false HHmmss
            - #### DateDto
                  dateS 起日
                  dateE 迄日
                  sec   秒數(可負數)
                  mins  分鐘(可負數)
                  hours 小時(可負數)
                  days  天數(可負數)
                  mons  月數(可負數)
                  years 年數(可負數)

                  getDateS2Integer(boolean bc) 取得起日整數(true 西元, false 民國)
                  getDateS2String (boolean bc) 取得起日字串(true 西元, false 民國)
                  getDateE2Integer(boolean bc) 取得起日整數(true 西元, false 民國)
                  getDateE2String (boolean bc) 取得起日字串(true 西元, false 民國)
                  getMonLimit() 取得起日當月份天數28,29,30,31
                  getYearLimit()取得起日當年份天數365, 366
            - #### dateDiff(DateDto dateDto)
                  日期運算 總共差幾年,差幾月,差幾天
            - #### dateDiffSp(DateDto dateDto)
                  日期運算 差幾年又幾月又幾天
            - #### getCalenderDay(DateDto dateDto)
                  日期運算 前後日差加減
- ### Exception
    - #### LogicException
        - #### throw new LogicException(String msgId, String msg);
            - #### msgid
                  錯誤訊息編號,可給五位或四位, 四位會自動補APTYPE
            - #### msg
                  錯誤訊息內容
- ### 交易事件可用參數
- ### 共用變數
    - #### MgGlobal (交易基本共用參數)



## 撰寫規則
- ### 程式與變數命名
    - #### 專案名稱(資料夾)一律小寫
    - #### 交易event與監聽器Class一律大駝峰並與專案名稱一致，監聽器後綴Lsnr
    - #### IO VO 物件名稱一律大寫並與TXCODE_FMTID相匹配
    - #### Mapper物件一律Mapper開頭並接續api名稱 大駝峰
    - #### 常數 一律大寫
    - #### 私有 小駝峰 class 大駝峰
    - #### DB Entity欄位變數一律小寫(DB本身全大寫)

- ### Log規範
    - #### 於class添加@Slf4j註釋
    - #### ex. ApLogHelper.info(log, false, LogType.TXCONTROL.getCode(), "TBSDY : {}, NBSDY : {}", tbsdy, nbsdy);
        - #### (logger, isSensitive, LogType, msg, ...arguments)
        - #### logger
            - #### logger object
        - #### isSensitive
            - #### true (敏感性資料)
            - #### false (非敏感性資料)
        - #### LogType
            - #### SYSTEM (系統)
            - #### NORMAL (其他)
            - #### UTIL (工具)
            - #### TXCONTROL (Txcontrol)
            - #### APLOG (業務)
            - #### EXCEPTION (錯誤)
            - #### BATCH (批次)
        - #### msg
            - #### 訊息內容
- ### 程式規範
    - #### 所有常數都必須宣告成static final 不可直接寫 \"xxx\"
    - #### 所有常數比較equals()必須反向ex this.HCODE.equals(g6100i.getHcode());
    - #### 變數轉型需透過共用程式Parse進行轉換
    - #### 除了單純整數(日期 Flag 序號....等)，其他數字型態皆需使用BigDecimal
    - #### 業務邏輯相關請撰寫於監聽器