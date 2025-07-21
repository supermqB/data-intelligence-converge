package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.model.dto.IncrConfigDTO;

import java.util.List;

/**
 * @author jinmengyu
 * @date 2024-01-17
 */
public interface IncrTimeService {

    void updateTableLatestTime(Long xdsId, List<IncrConfigDTO> incrConfigDTOList);
}
