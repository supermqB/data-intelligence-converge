package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.common.util.ShellUtil;
import com.lrhealth.data.converge.model.dto.ShellCommandDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shell脚本测试
 *
 * @author lr
 */
@Slf4j
@RestController()
@RequestMapping("/shell")
public class ShellController {

    /**
     * Shell脚本执行
     *
     * @return 执行结果
     */
    @PostMapping(value = "/exec")
    public String exec(@RequestBody ShellCommandDto command) {
        return ShellUtil.execCommand(command.getCommand());
    }
}
