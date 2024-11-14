package com.lrhealth.data.converge.controller;

import com.alibaba.fastjson.JSON;
import com.lrhealth.data.converge.kafka.factory.KafkaConsumerContext;
import com.lrhealth.data.converge.kafka.factory.KafkaDynamicConsumerFactory;
import com.lrhealth.data.converge.model.dto.DataSourceParamDto;
import com.lrhealth.data.converge.model.dto.FepScheduledDto;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2024-01-17
 */
@RestController()
@RequestMapping("/test")
public class UtilTestController {

    @Resource(name = "kafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.intelligence.tunnel-datasource-change}")
    private String tunnelChangeTopic;
    @Value("${spring.kafka.topic.intelligence.original-structure}")
    private String originalStructureTopic;
    @Value("${spring.kafka.topic.intelligence.fep-task}")
    private String fepTaskTopic;

    @GetMapping("/tunnel")
    public void sendTunnelChange(@RequestParam("tunnelId") Long tunnelId){
        List<Long> tunnelList = new ArrayList<>();
        tunnelList.add(tunnelId);
        kafkaTemplate.send(tunnelChangeTopic, JSON.toJSONString(tunnelList));
    }

    @GetMapping("/task")
    public void sendFepTask(@RequestParam("tunnelId") Long tunnelId,
                                      @RequestParam("taskId") Integer taskId){
        FepScheduledDto dto = FepScheduledDto.builder().tunnelId(tunnelId)
                .taskId(taskId).build();
        kafkaTemplate.send(fepTaskTopic, JSON.toJSONString(dto));
    }

    @GetMapping("/original")
    public void sendOriginalStructure(@RequestBody DataSourceParamDto dto){
        kafkaTemplate.send(originalStructureTopic, JSON.toJSONString(dto));
    }


    @Autowired
    private KafkaDynamicConsumerFactory factory;
    @Resource
    private KafkaConsumerContext consumerContext;

    @PostMapping("/send")
    public String send(@RequestParam("topic") String topic) {
        kafkaTemplate.send(topic, "hello!");
        return "发送完成！";
    }

    @GetMapping("/create/{groupId}")
    public String create(@PathVariable String groupId, @RequestParam("topic") String topic) {
        // 这里统一使用一个topic
        KafkaConsumer<String, String> consumer = factory.createConsumer(topic, groupId, "172.16.29.48:9092");
        consumerContext.addConsumerTask(topic, consumer);
        return "创建成功！";
    }

    @GetMapping("/remove/{groupId}")
    public String remove(@PathVariable String groupId) {
        consumerContext.removeConsumerTask(groupId);
        return "移除成功！";
    }
}
