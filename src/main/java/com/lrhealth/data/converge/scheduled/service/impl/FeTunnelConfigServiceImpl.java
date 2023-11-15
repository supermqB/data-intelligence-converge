package com.lrhealth.data.converge.scheduled.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lrhealth.data.common.exception.CommonException;
import com.lrhealth.data.converge.common.enums.TunnelCMEnum;
import com.lrhealth.data.converge.common.enums.TunnelStatusEnum;
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
import java.util.*;
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
        List<TunnelMessageDTO> tunnelMessageList = new ArrayList<>();
        List<ConvFeNode> fepList = getFepListByIpAndPort(ip, port);
        if (CollUtil.isEmpty(fepList)) return CollUtil.newArrayList();
        fepList.forEach(fep -> {
            List<ConvTunnel> tunnelList = tunnelService.list(new LambdaQueryWrapper<ConvTunnel>().eq(ConvTunnel::getFrontendId, fep.getId()));
            if (CollUtil.isEmpty(tunnelList)){
                return;
            }
            tunnelList.forEach(tunnel -> {
                TunnelMessageDTO tunnelMessageDTO = new TunnelMessageDTO();
                tunnelMessageDTO.setId(tunnel.getId());
                tunnelMessageDTO.setName(tunnel.getName());
                tunnelMessageDTO.setConvergeMethod(tunnel.getConvergeMethod());
                tunnelMessageDTO.setCronStr(tunnel.getCronStr());
                tunnelMessageDTO.setEncryptionFlag(tunnel.getEncryptionFlag());
                tunnelMessageDTO.setZipFlag(tunnel.getZipFlag());
                tunnelMessageDTO.setDataShardSize(tunnel.getDataShardSize());
                tunnelMessageDTO.setStatus(tunnel.getStatus());
                tunnelMessageDTO.setFileModeCollectDir(tunnel.getFileModeCollectDir());
                tunnelMessageDTO.setCollectRange(tunnel.getCollectRange());
                tunnelMessageDTO.setMqModeTopicName(tunnel.getMqModeTopicName());
                String action = (tunnel.getUpdateTime() == null) ? "add" : (Objects.equals(tunnel.getStatus(), TunnelStatusEnum.ABANDON.getValue()) ? "delete" : "update");
                tunnelMessageDTO.setAction(action);
                if (CharSequenceUtil.isNotBlank(tunnel.getJdbcUrl())){
                    JdbcInfoDto jdbcInfoDto = new JdbcInfoDto();
                    jdbcInfoDto.setJdbcUrl(tunnel.getJdbcUrl());
                    jdbcInfoDto.setDbUserName(tunnel.getDbUserName());
                    jdbcInfoDto.setDbPasswd(tunnel.getDbPasswd());
                    if (tunnel.getConvergeMethod().equals(TunnelCMEnum.LIBRARY_TABLE.getCode())){
                        // 库表采集范围和sql查询语句
                        List<TableInfoDto> tableInfoDtoList = new ArrayList<>();
                        List<String> tableList = Arrays.asList(tunnel.getCollectRange().split(","));
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
                    tunnelMessageDTO.setJdbcInfoDto(jdbcInfoDto);
                }
                tunnelMessageList.add(tunnelMessageDTO);
            });
        });
        return tunnelMessageList;
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
}
