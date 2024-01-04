package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.dto.OriginalStructureDto;
import com.lrhealth.data.converge.model.dto.OriginalTableCountDto;

/**
 * @author jinmengyu
 * @date 2024-01-04
 */
public interface ImportOriginalService {

    void importConvOriginal(OriginalStructureDto structureDto);

    void updateOriginalTableCount(OriginalTableCountDto tableCountDto);
}
