package com.lrhealth.data.converge.service.impl;

import com.lrhealth.data.converge.common.util.ShellUtil;
import com.lrhealth.data.converge.dao.entity.Xds;
import com.lrhealth.data.converge.service.ShellService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.text.CharPool.DOT;
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
    public String execShell(Xds fileXds){
        List<String> command = new ArrayList<>();
        String storedFileName = fileXds.getId() + DOT + fileXds.getOriFileType();
        command.add("cp");
        command.add(fileXds.getOriFileFromIp() + SLASH + fileXds.getOriFileName());
        command.add(fileXds.getStoredFilePath() + SLASH + storedFileName);
        ShellUtil.execCommand(command);
        List<String> mvCommand = new ArrayList<>();
        mvCommand.add("mv");
        mvCommand.add(fileXds.getOriFileFromIp() + SLASH + fileXds.getOriFileName());
        mvCommand.add(backupFilePath);
        ShellUtil.execCommand(mvCommand);
        return storedFileName;
    }



}
