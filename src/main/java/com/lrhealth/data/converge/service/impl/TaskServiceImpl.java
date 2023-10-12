package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lrhealth.data.common.enums.conv.ConvergeTypeEnum;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.model.*;
import com.lrhealth.data.converge.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
    @Resource
    private XdsInfoService xdsInfoService;
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
    private KafkaService kafkaService;

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
            kafkaService.xdsSendKafka(xds);
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
            kafkaService.xdsSendKafka(updatedXds);
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
        kafkaService.xdsSendKafka(result == null ? new Xds() : result);
        return result;
    }

}
