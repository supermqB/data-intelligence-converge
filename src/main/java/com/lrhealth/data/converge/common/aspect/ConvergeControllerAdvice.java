package com.lrhealth.data.converge.common.aspect;

import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Controller全局处理
 *
 * @author lr
 */
@ControllerAdvice
public class ConvergeControllerAdvice {
    private static final String SYS_USER_ROOT = "root";

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields(SYS_USER_ROOT);
    }


}