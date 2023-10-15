package com.lrhealth.data.converge.scheduled.thread;


import com.lrhealth.data.converge.common.util.SpringUtils;
import com.lrhealth.data.converge.scheduled.DownloadFileTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskLog;
import com.lrhealth.data.converge.scheduled.dao.service.impl.ConvTaskLogServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.TimerTask;

/**
 * 异步工厂（产生任务用）
 *
 * @author lr-app
 */
public class AsyncFactory
{
    private static final Logger log = LoggerFactory.getLogger(AsyncFactory.class);
    /**
     * 操作日志记录
     *
     * @param convTaskLog 操作日志信息
     * @return 任务task
     */
    public static TimerTask recordTaskLog(final ConvTaskLog convTaskLog)
    {
        return new TimerTask()
        {
            @Override
            public void run()
            {
                log.info(convTaskLog.getLogDetail());
                convTaskLog.setTimestamp(LocalDateTime.now());
                SpringUtils.getBean(ConvTaskLogServiceImpl.class).save(convTaskLog);
            }
        };
    }
}
