package com.lrhealth.data.converge.dao.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lrhealth.data.converge.model.SysDict;

import java.util.List;

/**
* @author zl
* @description 针对表【sys_dict(系统字典表)】的数据库操作Service
* @createDate 2024-01-02 15:50:35
*/
public interface SysDictService extends IService<SysDict> {

    /**
     * 获取数据库关键字
     * @param dbType 数据库类型
     * @return 关键字
     */
    List<SysDict> getDbKeyWords(String dbType);


}
