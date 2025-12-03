package com.bot.dataprocess;


public interface Job {
    /**
     * 執行入口方法
     */
    JobResult execute(JobContext ctx);
}
