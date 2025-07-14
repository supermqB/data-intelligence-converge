package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;

import java.util.List;
import java.util.concurrent.BlockingQueue;


/**
 * @author gaoshuai
 * @date 2027-07-08
 */
public interface ActiveInterfaceService {
    void activeInterfaceHandler(String topicKey, BlockingQueue<String> values);

    void initInterfaceDataConsumer(ConvTunnel tunnel);
}
