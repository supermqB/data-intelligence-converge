package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.FileConvergeInfoDTO;

/**
 * @author jinmengyu
 * @date 2023-08-08
 */
public interface ShellService {

    String execShell(FileConvergeInfoDTO file, Long xdsId);
}
