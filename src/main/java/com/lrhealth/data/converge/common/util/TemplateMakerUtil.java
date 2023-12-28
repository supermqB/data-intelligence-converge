package com.lrhealth.data.converge.common.util;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author jinmengyu
 * @date 2023-09-18
 */
@Slf4j
public class TemplateMakerUtil {

    // 模板配置对象
    private Configuration cfg;
    /**
     * 初始化配置
     */
    public void init() {
        cfg = new Configuration(Configuration.getVersion());
        try {
            cfg.setClassForTemplateLoading(this.getClass(), "/datax");
        } catch (Exception e) {
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
    }

    public void process(Map<String, Object> map, String frontendFilePath){
        try {
            Template template = cfg.getTemplate("csvWriter.ftl");
            template.process(map, new FileWriter(frontendFilePath));
        } catch (IOException | TemplateException e) {
            log.error("log error,{}", ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 解析字符串模板,通用方法
     *
     * @param template 字符串
     * @param model 数据
     * @param configuration 配置
     * @return 解析后内容
     */
    public static String process(String template, Map<String, ?> model, Configuration configuration) throws IOException, TemplateException {
        if (template == null) {
            return null;
        }
        if (configuration == null) {
            configuration = new Configuration();
        }
        StringWriter out = new StringWriter();
        new Template("template", new StringReader(template), configuration).process(model, out);
        return out.toString();
    }
}
