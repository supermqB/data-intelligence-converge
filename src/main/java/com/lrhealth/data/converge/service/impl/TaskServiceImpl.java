package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.common.enums.conv.ConvergeTypeEnum;
import com.lrhealth.data.common.enums.conv.KafkaSendFlagEnum;
import com.lrhealth.data.common.enums.conv.XdsStatusEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.util.file.LargeFileUtil;
import com.lrhealth.data.converge.dao.adpter.JDBCRepository;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.dao.service.XdsService;
import com.lrhealth.data.converge.model.*;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTaskResultView;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskResultViewService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTaskService;
import com.lrhealth.data.converge.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * 任务接口实现类
 * </p>
 *
 * @author lr
 * @since 2023/7/19
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {
    @Value("${spring.kafka.topic.xds}")
    private String topic;
    @Resource
    private XdsInfoService xdsInfoService;
    @Resource
    private KafkaTemplate<String, String> kafkaTemplate;
    @Resource
    private FepService fepService;
    @Resource
    private FileService fileService;
    @Resource
    private FlinkService flinkService;
    @Resource
    private DataXService dataXService;
    @Resource
    private ConvConfigService convConfigService;
    @Resource
    private ConvTaskResultViewService taskResultViewService;
    @Resource
    private XdsService xdsService;
    @Resource
    private ConvTaskService taskService;
    @Resource
    private JDBCRepository jdbcRepository;

    @Override
    public FileExecInfoDTO createTask(@RequestBody TaskDto taskDto) {
        // 校验参数
        if (CharSequenceUtil.isBlank(taskDto.getOdsTableName())){
            throw new CommonException("ods表值为空");
        }
        // 返回dataX抽取所需参数
        return dataXService.createTask(taskDto);
    }

    @Override
    public Xds updateTask(TaskDto taskDto) {
        Xds xds;
        //TODO: 目前任务状态在调度中写死TRUE,后续应该通过读取datax配置得到数据抽取结果
        if (taskDto.isTaskStatus()) {
            xds = dataXService.updateTask(taskDto);
            xdsSendKafka(xds);
        } else {
            xds = xdsInfoService.updateXdsFailure(taskDto.getXdsId(), "dataX数据抽取失败");
        }
        return xds;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void fepConverge(String projectId) {
        // 项目配置项，以及与前置机的关联
        FileExecInfoDTO fileExecInfoDTO = convConfigService.getConfig(projectId, null, 1);
        // 获得文件列表
        List<FileInfo> fepFileList = fepService.fepFileList(fileExecInfoDTO.getOriFilePath());
        fepFileList.forEach(fileInfo -> {
            // 新建xds
            Xds fileXds = xdsInfoService.createFileXds(fileExecInfoDTO);
            // 文件搬运
            FileConvergeInfoDTO fileConfig = new FileConvergeInfoDTO();
            BeanUtil.copyProperties(fileExecInfoDTO, fileConfig);
            fileConfig.setOriFileName(fileInfo.getFileName());
            fileConfig.setOriFileSize(BigDecimal.valueOf(fileInfo.getFileSize()));
            fileConfig.setOriFileType(fileInfo.getFileName().substring(fileInfo.getFileName().lastIndexOf(".")));
            Xds updatedXds = fileService.fileConverge(fileConfig, fileXds.getId());
            // 发送kafka
            xdsSendKafka(updatedXds);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Xds flinkConverge(FlinkTaskDto dto) {
        Xds result = null;
        FileExecInfoDTO fileExecInfoDTO = convConfigService.getConfig(null, dto.getSourceId(), dto.getType());
        if (ObjectUtil.isEmpty(fileExecInfoDTO)){
            throw new CommonException("flink关联配置为空");
        }
        Xds flinkXds = xdsInfoService.createFlinkXds(dto, fileExecInfoDTO);
        if (ConvergeTypeEnum.isDataBase(dto.getType())){
            result = flinkService.database(flinkXds);
        }else if (ConvergeTypeEnum.isFile(dto.getType())){
            result = flinkService.file(fileExecInfoDTO, flinkXds.getId(), dto.getFilePath());
        }
        xdsSendKafka(result == null ? new Xds() : result);
        return result;
    }



    /**
     * 发送kafka消息
     *
     * @param xds XDS信息
     */
    private void xdsSendKafka(Xds xds) {
        if (ObjectUtil.isNull(xds)){
            throw new CommonException("kafka send xds is null");
        }
        Xds checkXds = xdsInfoService.getById(xds.getId());
        if (!KafkaSendFlagEnum.isSent(xds.getKafkaSendFlag())) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id", checkXds.getId());
            kafkaTemplate.send(topic, JSON.toJSONString(map));
            xdsInfoService.updateKafkaSent(xds);
        }
    }


    @Override
    @Async
    public void fileParseAndSave(Integer taskResultViewId) {
        // 创建xds
        Xds xds = createXds(taskResultViewId);
        Integer countNumber = LargeFileUtil.csvParseAndInsert(xds.getStoredFilePath(), xds.getStoredFileName(), xds.getId(), xds.getOdsTableName());
        // 获得数据的大概存储大小
        String avgRowLength = getAvgRowLength(xds.getOdsTableName());
        // 更新xds
        updateXds(xds.getId(), countNumber * Long.parseLong(avgRowLength));
        // 发送kafka
        xdsSendKafka(xds);
    }

    private Xds createXds(Integer taskResultViewId){
        ConvTaskResultView taskResultView = taskResultViewService.getById(taskResultViewId);
        ConvTask convTask = taskService.getById(taskResultView.getTaskId());
        Xds xds =  Xds.builder()
                .id(IdUtil.getSnowflakeNextId())
                .orgCode(convTask.getOrgCode())
                .sysCode(convTask.getSysCode())
                .convergeMethod(convTask.getConvergeMethod())
                .dataConvergeStartTime(convTask.getStartTime())
                .dataConvergeStatus(XdsStatusEnum.INIT.getCode())
                .odsModelName(taskResultView.getTableName())
                .oriFileName(taskResultView.getFeStoredFilename())
                .storedFilePath(taskResultView.getStoredPath())
                .storedFileName(taskResultView.getFeStoredFilename())
                .storedFileType("csv")
                .storedFileMode(0)
                .odsTableName(convTask.getSysCode() + "_" + taskResultView.getTableName())
                .storedFileSize(BigDecimal.valueOf(taskResultView.getDataSize()))
                .dataCount(taskResultView.getDataItemCount())
                .createTime(LocalDateTime.now())
                .build();
        xdsService.save(xds);
        return xdsService.getById(xds.getId());
    }

    private void updateXds(Long xdsId, Long dataSize){
        Xds updateXds = Xds.builder().id(xdsId).dataSize(dataSize)
                .dataConvergeEndTime(LocalDateTime.now()).updateTime(LocalDateTime.now()).build();
        xdsService.updateById(updateXds);
    }

    private String getAvgRowLength(String odsTableName){
        // 刷新tables表的数据
        String refreshSql = "ANALYZE TABLE " + odsTableName + " COMPUTE STATISTICS FOR ALL COLUMNS SIZE AUTO;";
        jdbcRepository.execSql(refreshSql);
        // 获取每行的平均大小
        String selectSql = "select AVG_ROW_LENGTH from information_schema.TABLES where TABLE_NAME = '" + odsTableName + "';";
        return jdbcRepository.execSql(selectSql);
    }

}
