package com.lrhealth.data.converge.service.impl;

import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.enums.conv.KafkaSendFlagEnum;
import com.lrhealth.data.converge.dao.entity.ConvergeConfig;
import com.lrhealth.data.converge.dao.entity.ProjectConvergeRelation;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.ConvergeConfigService;
import com.lrhealth.data.converge.model.TaskDto;
import com.lrhealth.data.converge.service.ProjectConvergeService;
import com.lrhealth.data.converge.service.TaskService;
import com.lrhealth.data.converge.service.XdsInfoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * <p>
 * 任务接口实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@Service
public class TaskServiceImpl implements TaskService {
    @Value("${spring.kafka.topic.xds}")
    private String topic;
    @Resource
    private XdsInfoService xdsInfoService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private ProjectConvergeService proConvService;
    @Resource
    private ConvergeConfigService configService;

    @Override
    public Xds createTask(@RequestBody TaskDto taskDto) {
        ConvergeConfig config = getConfig(taskDto.getProjectId());
        return xdsInfoService.createXdsInfo(taskDto, config);
    }

    @Override
    public Xds updateTask(TaskDto taskDto) {
        Xds xds;
        if (taskDto.isTaskStatus()) {
            xds = xdsInfoService.updateXdsCompleted(taskDto.getXdsId());
        } else {
            xds = xdsInfoService.updateXdsFailure(taskDto.getXdsId(), "dataX数据抽取失败");
        }
        return xds;
    }


    /**
     * 发送kafka消息
     *
     * @param xds XDS信息
     */
    private void xdsSendKafka(Xds xds) {
        Xds checkXds = xdsInfoService.getById(xds.getId());
        if (!KafkaSendFlagEnum.isSent(xds.getKafkaSendFlag())) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", checkXds.getId());
            kafkaTemplate.send(topic, JSON.toJSONString(map));
            xdsInfoService.updateKafkaSent(xds);
        }
    }

    /**
     * 获取配置信息
     *
     * @param projId 项目ID
     * @return 配置信息
     */
    private ConvergeConfig getConfig(String projId) {
        ProjectConvergeRelation relation = proConvService.getByProjId(projId);
        return configService.getById(relation.getConvergeId());
    }
}
