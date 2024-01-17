package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.Xds;

/**
 * @author jinmengyu
 * @date 2024-01-17
 */
public interface IncrTimeService {

    void updateTableLatestTime(Xds xds);
}
