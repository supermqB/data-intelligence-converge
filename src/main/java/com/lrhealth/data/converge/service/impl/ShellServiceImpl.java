package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.net.NetUtil;
import com.lrhealth.data.converge.common.util.ShellUtil;
import com.lrhealth.data.converge.model.FepFileInfoVo;
import com.lrhealth.data.converge.service.ShellService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.text.StrPool.SLASH;

/**
 * @author jinmengyu
 * @date 2023-08-08
 */
@Service
@Slf4j
public class ShellServiceImpl implements ShellService {


    @Value("${converge.backup}")
    private String backupFilePath;

    @Override
    public String execShell(FepFileInfoVo fepFileInfoVo) {
        String storedFileName = fepFileInfoVo.getXdsId() + "." + fepFileInfoVo.getOriFileType();
        String oriFilePath = fepFileInfoVo.getOriFileFromPath() + fepFileInfoVo.getOriFileName();
//        String storedFilePath = fepFileInfoVo.getStoredFilePath() + SLASH + storedFileName;
        String storedFilePath = fepFileInfoVo.getStoredFilePath();
        File file = new File(storedFilePath);
        if (!file.exists()) {
            log.info("目录不存在，创建目录");
            file.mkdirs();
        }
        if (fepFileInfoVo.getFrontendIp().equals(NetUtil.getLocalhostStr())){
            cpExecShell(oriFilePath, storedFilePath, storedFileName);
        } else {
            scpExecShell(oriFilePath, storedFilePath, fepFileInfoVo, storedFileName);
        }
        return storedFileName;
    }

    private void cpExecShell(String oriFilePath, String storedFilePath, String storedFileName){
        List<String> command = new ArrayList<>();
        command.add("cp");
        command.add(oriFilePath);
        command.add(storedFilePath + SLASH + storedFileName);
        ShellUtil.execCommand(command);
        List<String> mvCommand = new ArrayList<>();
        mvCommand.add("mv");
        mvCommand.add(oriFilePath);
        mvCommand.add(backupFilePath);
        ShellUtil.execCommand(mvCommand);
    }

    private void scpExecShell(String oriFilePath, String storedFilePath, FepFileInfoVo fepFileInfoVo, String storedFileName){
        String sshpass = "sshpass -p '" + fepFileInfoVo.getFrontendPwd() + "' ";
        String fepMessage = fepFileInfoVo.getFrontendPort() + " " + fepFileInfoVo.getFrontendUsername() + "@" + fepFileInfoVo.getFrontendIp();
        List<String> command = new ArrayList<>();
        // sshpass -p 'password' scp -P 29022 rdcp@172.16.29.60:sourceFilePath(远程服务器原始路径) targetFilePath(汇聚服务器存储路径)
        command.add(sshpass + "scp -P " + fepMessage + ":" + oriFilePath + " " + storedFilePath + SLASH + storedFileName);
        ShellUtil.execCommand(command);
        List<String> mvCommand = new ArrayList<>();
        // sshpass -p '1q2w3e!Q@W#ERDCP' ssh -p 29022 rdcp@172.16.29.60 "mv 远程服务器原始路径 远程服务器备份路径"
        mvCommand.add(sshpass + "ssh -p " + fepMessage + "mv " + oriFilePath + " " + backupFilePath);
        ShellUtil.execCommand(mvCommand);
    }

}
