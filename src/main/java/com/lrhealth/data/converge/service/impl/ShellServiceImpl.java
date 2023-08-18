package com.lrhealth.data.converge.service.impl;

import cn.hutool.core.net.NetUtil;
import com.lrhealth.data.converge.common.util.ShellUtil;
import com.lrhealth.data.converge.model.FepFileInfoVo;
import com.lrhealth.data.converge.service.ShellService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.text.StrPool.SLASH;

/**
 * @author jinmengyu
 * @date 2023-08-08
 */
@Service
public class ShellServiceImpl implements ShellService {


    @Value("${converge.backup}")
    private String backupFilePath;

    @Override
    public String execShell(FepFileInfoVo fepFileInfoVo) {
        String storedFileName = fepFileInfoVo.getXdsId() + "." + fepFileInfoVo.getOriFileType();
        String oriFilePath = fepFileInfoVo.getOriFileFromPath() + fepFileInfoVo.getOriFileName();
//        String storedFilePath = fepFileInfoVo.getStoredFilePath() + SLASH + storedFileName;
        String storedFilePath = fepFileInfoVo.getStoredFilePath();
        if (fepFileInfoVo.getFrontendIp().equals(NetUtil.getLocalhostStr())){
            cpExecShell(oriFilePath, storedFilePath, storedFileName);
        } else {
            scpExecShell(oriFilePath, storedFilePath, fepFileInfoVo, storedFileName);
        }
        return storedFileName;
    }

    private void cpExecShell(String oriFilePath, String storedFilePath, String storedFileName){
        // 解决解析目标目录不存在问题，先创建目录(暂时)
        List<String> mkdirCommand = new ArrayList<>();
        mkdirCommand.add("mkdir -p");
        mkdirCommand.add(storedFilePath);
        ShellUtil.execCommand(mkdirCommand);
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
        List<String> mkdirCommand = new ArrayList<>();
        mkdirCommand.add("mkdir -p");
        mkdirCommand.add(storedFilePath);
        ShellUtil.execCommand(mkdirCommand);
        String sshpass = "sshpass -p '" + fepFileInfoVo.getFrontendPwd() + "' ";
        String fepMessage = "-p " + fepFileInfoVo.getFrontendPort() + " " + fepFileInfoVo.getFrontendUsername() + "@" + fepFileInfoVo.getFrontendIp();
        List<String> command = new ArrayList<>();
        // sshpass -p 'password' scp -p 29022 rdcp@172.16.29.60:sourceFilePath(远程服务器原始路径) targetFilePath(汇聚服务器存储路径)
        command.add(sshpass + "scp");
        command.add(fepMessage + ":" + oriFilePath);
        command.add(storedFilePath + SLASH + storedFileName);
        ShellUtil.execCommand(command);
        List<String> mvCommand = new ArrayList<>();
        // sshpass -p '1q2w3e!Q@W#ERDCP' ssh -p 29022 rdcp@172.16.29.60 "mv 远程服务器原始路径 远程服务器备份路径"
        mvCommand.add(sshpass);
        mvCommand.add("ssh " + fepMessage);
        mvCommand.add("mv");
        mvCommand.add(oriFilePath);
        mvCommand.add(backupFilePath);
        ShellUtil.execCommand(mvCommand);
    }

}
