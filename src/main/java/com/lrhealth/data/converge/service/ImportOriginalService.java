package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.dto.DataSourceInfoDto;
import com.lrhealth.data.converge.model.dto.OriDataProbeDTO;
import com.lrhealth.data.converge.model.dto.OriginalStructureDto;

/**
 * @author jinmengyu
 * @date 2024-01-04
 */
public interface ImportOriginalService {

    void importPlatformDataType(DataSourceInfoDto dto);

    void importConvOriginal(OriginalStructureDto structureDto);

    void originalTableProbe(OriDataProbeDTO oriDataProbeDTO);
}
