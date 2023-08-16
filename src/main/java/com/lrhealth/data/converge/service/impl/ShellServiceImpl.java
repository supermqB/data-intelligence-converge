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
        String oriFilePath = fepFileInfoVo.getOriFileFromPath() + SLASH + fepFileInfoVo.getOriFileName();
        String storedFilePath = fepFileInfoVo.getStoredFilePath() + SLASH + storedFileName;
        if (fepFileInfoVo.getFrontendIp().equals(NetUtil.getLocalhostStr())){
            cpExecShell(oriFilePath, storedFilePath);
        } else {
            scpExecShell(oriFilePath, storedFilePath, fepFileInfoVo);
        }
        return storedFileName;
    }

    private void cpExecShell(String oriFilePath, String storedFilePath){
        List<String> command = new ArrayList<>();
        command.add("cp");
        command.add(oriFilePath);
        command.add(storedFilePath);
        ShellUtil.execCommand(command);
        List<String> mvCommand = new ArrayList<>();
        mvCommand.add("mv");
        mvCommand.add(oriFilePath);
        mvCommand.add(backupFilePath);
        ShellUtil.execCommand(mvCommand);
    }

    private void scpExecShell(String oriFilePath, String storedFilePath,FepFileInfoVo fepFileInfoVo){
        String sshpass = "sshpass -p '" + fepFileInfoVo.getFrontendPwd() + "' ";
        String fepMessage = "-P " + fepFileInfoVo.getFrontendPort() + " " + fepFileInfoVo.getFrontendUsername() + "@" + fepFileInfoVo.getFrontendIp();
        List<String> command = new ArrayList<>();
        // sshpass -p 'password' scp -P 29022 rdcp@172.16.29.60:/data/app/rdcp/ds/test.xlsx /data/app/rdcp/lr-rd-rdcp-data-converge/test.xlsx
        command.add(sshpass);
        command.add("scp " + fepMessage + ":");
        command.add(oriFilePath);
        command.add(storedFilePath);
        ShellUtil.execCommand(command);
        List<String> mvCommand = new ArrayList<>();
        // sshpass -p '1q2w3e!Q@W#ERDCP' ssh -p 29022 rdcp@172.16.29.60 "mv /data/app/rdcp/ds/test.xlsx /data/app/rdcp/ds/21222.xlsx"
        mvCommand.add(sshpass);
        mvCommand.add("ssh " + fepMessage + " ");
        mvCommand.add("mv");
        mvCommand.add(oriFilePath);
        mvCommand.add(backupFilePath);
        ShellUtil.execCommand(mvCommand);
    }

}
