package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.common.enums.DsStatus;
import com.lrhealth.data.converge.common.util.DsPageInfo;
import com.lrhealth.data.converge.common.util.DsResult;
import com.lrhealth.data.converge.model.ConvergeConfigDto;
import com.lrhealth.data.converge.service.ProjectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 任务调度-项目相关接口
 * </p>
 *
 * @author lr
 * @since 2023/7/21
 */
@RestController()
@RequestMapping("/proj")
public class ProjectController {
    @Resource
    private ProjectService projectService;

    /**
     * 查询汇聚配置信息
     *
     * @param pageSize 分页-单页数据条数
     * @param pageNo   分页-当前页
     * @return 汇聚配置信息
     */
    @GetMapping(value = "page")
    public DsResult<DsPageInfo<ConvergeConfigDto>> listConvConfig(@RequestParam("pageSize") Integer pageSize,
                                                                  @RequestParam("pageNo") Integer pageNo) {
        DsResult<DsPageInfo<ConvergeConfigDto>> result = new DsResult<>();
        DsPageInfo<ConvergeConfigDto> page = projectService.queryConvConfigPage(pageSize, pageNo);
        result.setData(page);
        result.setCode(DsStatus.SUCCESS.getCode());
        result.setMsg(DsStatus.SUCCESS.getMsg());
        return result;
    }

    /**
     * 保存项目-配置信息关系
     *
     * @param projectId    项目ID
     * @param convConfigId 配置ID
     * @return 关联关系ID
     */
    @PostMapping("save")
    public DsResult<Long> bindProjectAndConvConfig(String projectId, Long convConfigId) {
        DsResult<Long> result = new DsResult<>();
        try {
            Long relationId = projectService.bindProjectAndConvConfig(projectId, convConfigId);
            result.setData(relationId);
            result.setCode(DsStatus.SUCCESS.getCode());
            result.setMsg(DsStatus.SUCCESS.getMsg());
        } catch (Exception e) {
            result.setCode(DsStatus.ERROR.getCode());
            result.setMsg(e.getMessage());
        }
        return result;
    }

    @PostMapping("delete")
    public void deleteProjectConvergeRelation(String projectId){
        projectService.deleteProjectConvRelation(projectId);
    }
}
