package com.lrhealth.data.converge.service.impl;

import com.alibaba.fastjson2.JSON;
import com.lrhealth.data.converge.cache.Cache;
import com.lrhealth.data.converge.model.dto.FepScheduledDto;
import com.lrhealth.data.converge.service.ScheduleTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2023-12-07
 */
@Slf4j
@Service
public class ScheduleTaskServiceImpl implements ScheduleTaskService {

    @Resource
    private Cache taskCache;


    @Override
    public List<FepScheduledDto> getCacheTask(String ip, Integer port) {

        List<String> allTaskList = taskCache.getAllByPrefix(ip + "-" + port);
        List<FepScheduledDto> returnList = new ArrayList<>();
        allTaskList.forEach(fepPreFix -> {
            try {
                Object dto = taskCache.getObject(fepPreFix);
                FepScheduledDto scheduledDto = JSON.parseObject(JSON.toJSONString(dto), FepScheduledDto.class);
                returnList.add(scheduledDto);
                taskCache.removeObject(fepPreFix);
            }catch (Exception e){
                log.error("fep schedule parse error, {}", ExceptionUtils.getStackTrace(e));
            }
        });
        return returnList;
    }
}
