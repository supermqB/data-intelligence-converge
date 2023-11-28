package com.lrhealth.data.converge.controller;

import com.lrhealth.data.converge.common.util.ResResult;
import com.lrhealth.data.converge.model.dto.ConvData;
import com.lrhealth.data.converge.product.CdcConvDataProduct;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * FlinkCDC数据接收接口
 * </p>
 *
 * @author lr
 * @since 2023/11/28 21:37
 */
@RestController
@RequestMapping("cdc")
public class FlinkConvergeController {
    @Resource
    private CdcConvDataProduct cdcConvDataProduct;

    /**
     * Flink CDC数据汇聚接口
     *
     * @return 当前时间
     */
    @PostMapping(value = "/conv")
    public ResResult<Void> monitor(@RequestBody List<ConvData> convDataList) {
        cdcConvDataProduct.send(convDataList);
        return ResResult.success();
    }
}
