package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;

import java.util.List;


/**
 * @author gaoshuai
 * @date 2027-07-08
 */
public interface ActiveInterfaceService {
    void activeInterfaceHandler(String topicKey, List<String> value);

    void initInterfaceDataConsumer(ConvTunnel tunnel);
}
