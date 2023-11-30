package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.LibraryTableModelEnum;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.scheduled.DownloadFileTask;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvFeNode;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvFeNodeService;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.model.dto.*;
import com.lrhealth.data.converge.scheduled.service.ConvergeService;
import com.lrhealth.data.converge.scheduled.service.FeTunnelConfigService;
import com.lrhealth.data.model.original.model.OriginalModel;
import com.lrhealth.data.model.original.model.OriginalModelColumn;
import com.lrhealth.data.model.original.service.OriginalModelColumnService;
import com.lrhealth.data.model.original.service.OriginalModelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jinmengyu
 * @date 2023-11-14
 */
@Slf4j
@Service
public class FeTunnelConfigServiceImpl implements FeTunnelConfigService {

    @Resource
    private ConvTunnelService tunnelService;
    @Resource
    private ConvFeNodeService feNodeService;
    @Resource
    private OriginalModelService originalModelService;
    @Resource
    private OriginalModelColumnService originalModelColumnService;
    @Resource
    private ConvergeService convergeService;


    @Override
    public List<TunnelMessageDTO> getFepTunnelConfig(String ip, Integer port) {
        List<TunnelMessageDTO> messageDTOList = new ArrayList<>();
        // 查询到所有创建的前置机
        List<ConvFeNode> fepList = getFepListByIpAndPort(ip, port);
        if (CollUtil.isEmpty(fepList)) return CollUtil.newArrayList();
        // 组装管道信息
        fepList.forEach(fep -> feNodeTunnelConfig(messageDTOList, fep));
        return messageDTOList;
    }

    @Override
    public void updateFepStatus(ActiveFepUploadDto activeFepUploadDto) {
        FrontendStatusDto frontendStatusDto = activeFepUploadDto.getFrontendStatusDto();
        List<ConvFeNode> fepList = getFepListByIpAndPort(activeFepUploadDto.getIp(), activeFepUploadDto.getPort());
        if (fepList.isEmpty()) return;
        if (frontendStatusDto == null || frontendStatusDto.getTunnelStatusDtoList() == null) {
            log.error("fep status返回结果异常: " + frontendStatusDto);
            return;
        }
        convergeService.updateFepStatus(frontendStatusDto, DownloadFileTask.taskDeque);
    }

    private List<ConvFeNode> getFepListByIpAndPort(String ip, Integer port){
        if (CharSequenceUtil.isBlank(ip)){
            throw new CommonException("请输入前置机ip地址");
        }
        List<ConvFeNode> fepList = feNodeService.list(new LambdaQueryWrapper<ConvFeNode>().eq(ConvFeNode::getIp, ip)
                .eq((port != null), ConvFeNode::getPort, port)
                .eq(ConvFeNode::getDelFlag, 0));
        if (CollUtil.isEmpty(fepList)){
            return Collections.emptyList();
        }
        return fepList;
    }


    private void feNodeTunnelConfig(List<TunnelMessageDTO> messageDTOList, ConvFeNode fep){
        List<ConvTunnel> tunnelList = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>().eq(ConvTunnel::getFrontendId, fep.getId()));
        if (CollUtil.isEmpty(tunnelList)){
            return;
        }
        tunnelList.forEach(tunnel -> {
            TunnelMessageDTO tunnelMessageDTO = new TunnelMessageDTO();
            // 管道基本信息
            BeanUtil.copyProperties(tunnel, tunnelMessageDTO);
            if (tunnel.getConvergeMethod().equals(TunnelCMEnum.LIBRARY_TABLE.getCode())
            || tunnel.getConvergeMethod().equals(TunnelCMEnum.CDC_LOG.getCode())){
                // 库表/日志的读库信息
                JdbcInfoDto jdbcInfoDto = new JdbcInfoDto();
                jdbcInfoDto.setJdbcUrl(tunnel.getJdbcUrl());
                jdbcInfoDto.setDbUserName(tunnel.getDbUserName());
                jdbcInfoDto.setDbPasswd(tunnel.getDbPasswd());
                // 库表采集
                if (tunnel.getConvergeMethod().equals(TunnelCMEnum.LIBRARY_TABLE.getCode())){
                    // 全量/增量采集
                    jdbcInfoDto.setColType(tunnel.getColType());
                    jdbcInfoDto.setFullColStartTime(tunnel.getFullColStartTime());
                    jdbcInfoDto.setFullColEndTime(tunnel.getFullColEndTime());
                    // 库到库/库到文件
                    jdbcInfoDto.setCollectModel(tunnel.getCollectModel());
                    // 库到库
                    if(LibraryTableModelEnum.DATABASE_TO_DATABASE.getCode().equals(jdbcInfoDto.getCollectModel())){
                        jdbcInfoDto.setJdbcUrlForIn(tunnel.getJdbcUrlForIn());
                        jdbcInfoDto.setDbUserNameForIn(tunnel.getDbUserNameForIn());
                        jdbcInfoDto.setDbPasswdForIn(tunnel.getDbPasswdForIn());
                    }
                    // 表以及对应的sql信息
                    assembleTableInfoMessage(tunnel.getCollectRange(), jdbcInfoDto);
                }
                tunnelMessageDTO.setJdbcInfoDto(jdbcInfoDto);
            }
            messageDTOList.add(tunnelMessageDTO);
        });
    }


    private void assembleTableInfoMessage(String collectRange, JdbcInfoDto jdbcInfoDto){
        // 库表采集范围和sql查询语句
        List<TableInfoDto> tableInfoDtoList = new ArrayList<>();
        List<String> tableList = Arrays.asList(collectRange.split(","));
        List<OriginalModel> modelList = originalModelService.list(new LambdaQueryWrapper<OriginalModel>()
                .in(OriginalModel::getNameEn, tableList).eq(OriginalModel::getDelFlag, 0));
        modelList.forEach(model -> {
            TableInfoDto tableInfoDto = new TableInfoDto();
            tableInfoDto.setTableName(model.getNameEn());
            tableInfoDto.setSqlQuery(model.getModelQuerySql());
            List<OriginalModelColumn> seqColumns = originalModelColumnService.list(new LambdaQueryWrapper<OriginalModelColumn>().eq(OriginalModelColumn::getModelId, model.getId()).eq(OriginalModelColumn::getSeqFlag, "1").eq(OriginalModelColumn::getDelFlag, 0));
            tableInfoDto.setSeqFields(seqColumns.stream().map(OriginalModelColumn::getNameEn).collect(Collectors.toList()));
            tableInfoDtoList.add(tableInfoDto);
        });
        jdbcInfoDto.setTableInfoDtoList(tableInfoDtoList);
    }
}
