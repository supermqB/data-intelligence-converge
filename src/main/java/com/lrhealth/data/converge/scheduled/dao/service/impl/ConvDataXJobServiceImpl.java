package com.lrhealth.data.converge.scheduled.dao.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvDataXJob;
import com.lrhealth.data.converge.scheduled.dao.mapper.ConvDataxJobMapper;
import com.lrhealth.data.converge.scheduled.dao.service.ConvDataXJobService;
import com.lrhealth.data.converge.scheduled.model.dto.TableInfoDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author jinmengyu
 * @since 2023-09-14
 */
@Service
public class ConvDataXJobServiceImpl extends ServiceImpl<ConvDataxJobMapper, ConvDataXJob> implements ConvDataXJobService {

    @Override
    public ConvDataXJob createOrUpdateDataXJob(String jsonPath, Long tunnelId, String tableName, TableInfoDto tableInfoDto) {
        List<ConvDataXJob> jobList = this.list(new LambdaQueryWrapper<ConvDataXJob>().eq(ConvDataXJob::getTunnelId, tunnelId)
                .eq(ConvDataXJob::getTableName, tableName));
        if (ObjectUtil.isEmpty(jobList)) {
            // 创建
            ConvDataXJob dataXJob = ConvDataXJob.builder()
                    .id(IdUtil.getSnowflakeNextId())
                    .jobMode("Standalone")
                    .tunnelId(tunnelId)
                    .jsonPath(jsonPath)
                    .tableName(tableName)
                    .sqlQuery(tableInfoDto.getSqlQuery())
                    .seqFields(ObjectUtil.isEmpty(tableInfoDto.getSeqFields()) ?  null : tableInfoDto.getSeqFields().get(0))
                    .delFlag(0)
                    .createTime(LocalDateTime.now())
                    .build();
            this.save(dataXJob);
        }else {
            // 更新
            this.updateById(ConvDataXJob.builder().id(jobList.get(0).getId()).sqlQuery(tableInfoDto.getSqlQuery())
                    .seqFields(ObjectUtil.isEmpty(tableInfoDto.getSeqFields()) ?  null : tableInfoDto.getSeqFields().get(0))
                    .updateTime(LocalDateTime.now()).build());
        }
        return this.getOne(new LambdaQueryWrapper<ConvDataXJob>().eq(ConvDataXJob::getTunnelId, tunnelId)
                .eq(ConvDataXJob::getTableName, tableName));
    }

}
