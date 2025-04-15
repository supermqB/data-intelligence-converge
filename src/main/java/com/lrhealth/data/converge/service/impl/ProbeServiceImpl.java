package com.lrhealth.data.converge.service.impl;

import com.lrhealth.data.converge.common.enums.ProbeModelEnum;
import com.lrhealth.data.converge.dao.entity.ConvOriginalProbe;
import com.lrhealth.data.converge.dao.service.ConvOriginalProbeService;
import com.lrhealth.data.converge.model.dto.ColumnValueUpDTO;
import com.lrhealth.data.converge.model.dto.DictValueDTO;
import com.lrhealth.data.converge.service.ProbeService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jinmengyu
 * @date 2025-04-15
 */
@Service
public class ProbeServiceImpl implements ProbeService {

    @Resource
    private ConvOriginalProbeService originalProbeService;

    @Override
    public void saveColumnValueFreq(ColumnValueUpDTO valueUpDTO) {
        List<DictValueDTO> valueDTOList = valueUpDTO.getValueDTOList();
        List<ConvOriginalProbe> probeList = new ArrayList<>();
        for (DictValueDTO valueDTO : valueDTOList){
            ConvOriginalProbe probe = ConvOriginalProbe.builder()
                    .probeModel(ProbeModelEnum.COLUMN_VALUEFREQ.getCode())
                    .tableId(valueUpDTO.getTableId())
                    .tableName(valueUpDTO.getTableName())
                    .columnId(valueUpDTO.getColumnId())
                    .columnName(valueUpDTO.getColumnName())
                    .columnValue(valueDTO.getValue())
                    .valueFreq(Double.valueOf(valueDTO.getFrequency()))
                    .build();
            probeList.add(probe);
        }
        originalProbeService.saveBatch(probeList);
    }
}
