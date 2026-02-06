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