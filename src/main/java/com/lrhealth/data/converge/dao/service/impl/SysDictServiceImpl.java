package com.lrhealth.data.converge.dao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lrhealth.data.converge.dao.mapper.SysDictMapper;
import com.lrhealth.data.converge.dao.service.SysDictService;
import com.lrhealth.data.converge.model.SysDict;
import org.springframework.stereotype.Service;

import java.util.List;

/**
* @author zl
* @description 针对表【sys_dict(系统字典表)】的数据库操作Service实现
* @createDate 2024-01-02 15:50:35
*/
@Service
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict>
    implements SysDictService {


    @Override
    public List<SysDict> getDbKeyWords(String dictCode) {
        return this.list(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictCode, dictCode).eq(SysDict::getDelFlag, 0));
    }
}




