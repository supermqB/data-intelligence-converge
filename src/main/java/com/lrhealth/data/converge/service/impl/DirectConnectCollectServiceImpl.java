package com.lrhealth.data.converge.service.impl;

import cn.hutool.crypto.asymmetric.KeyType;
import cn.hutool.crypto.asymmetric.RSA;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson.JSON;
import com.lrhealth.data.converge.scheduled.dao.entity.ConvTunnel;
import com.lrhealth.data.converge.scheduled.dao.service.ConvTunnelService;
import com.lrhealth.data.converge.scheduled.model.dto.TunnelMessageDTO;
import com.lrhealth.data.converge.scheduled.service.FeTunnelConfigService;
import com.lrhealth.data.converge.scheduled.utils.RsaUtils;
import com.lrhealth.data.converge.service.DirectConnectCollectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author jinmengyu
 * @date 2023-12-19
 */
@Slf4j
@Service
public class DirectConnectCollectServiceImpl implements DirectConnectCollectService {
    @Value("${lrhealth.converge.privateKeyStr}")
    private String privateKeyStr;

    @Resource
    private FeTunnelConfigService feTunnelConfigService;
    @Resource
    private ConvTunnelService convTunnelService;

    @Override
    public void tunnelConfig(TunnelMessageDTO dto) {
        // 汇聚根据tunnelId去重新获取一次配置代替入参的dto
        ConvTunnel tunnel = convTunnelService.getById(dto.getId());
        TunnelMessageDTO tunnelMessageDTO = feTunnelConfigService.getTunnelMessage(tunnel);
        String url = "localhost:18081/task/tunnel/upsert";
        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();
        log.info("同步直连管道配置: {}", url);
        try {
            String body = JSON.toJSONString(tunnelMessageDTO);
            log.debug("同步直连管道配置: " + url + body);
            HttpResponse response = HttpRequest
                    .post(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .body(body)
                    .timeout(3000)
                    .contentType("application/json;charset=UTF-8")
                    .execute();
            int httpStatus = response.getStatus();
            if (httpStatus != 200)
                log.error("同步直连配置错误：" + response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void tunnelExec(Integer taskId, Long tunnelId) {
        String url = "localhost:18081/task/tunnel/exec";

        RSA instance = RsaUtils.getInstance(privateKeyStr);
        String token = "lrhealth:" + System.currentTimeMillis();
        try {
            log.debug("直连调取前置机执行: " + url + " tunnelId: " + tunnelId + " fe taskId: " + taskId);
            HttpResponse response = HttpRequest.post(url)
                    .header("Authorization", instance.encryptBase64(token, KeyType.PrivateKey))
                    .form("tunnelId", tunnelId == null ? "" : String.valueOf(tunnelId))
                    .form("taskId", taskId == null ? "" : String.valueOf(taskId))
                    .timeout(3000)
                    .execute();
            int httpStatus = response.getStatus();
            if (httpStatus != 200)
                log.error("直连调度调取失败：" + response);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
