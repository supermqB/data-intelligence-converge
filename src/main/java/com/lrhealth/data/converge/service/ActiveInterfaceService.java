package com.lrhealth.data.converge.service;

import java.util.List;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;

/**
 * @author gaoshuai
 * @date 2027-07-08
 */
public interface ActiveInterfaceService {
    void activeInterfaceHandler(String topicKey, List<String> values);

    void initInterfaceDataConsumer(ConvTunnel tunnel);
}
