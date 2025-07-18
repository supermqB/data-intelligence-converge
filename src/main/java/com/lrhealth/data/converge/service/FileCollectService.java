package com.lrhealth.data.converge.service;

import com.lrhealth.data.converge.dao.entity.ConvTunnel;

import java.util.List;

public interface FileCollectService {
    void initFileDataConsumer(ConvTunnel tunnel);

    void fileDataHandler(String topicKey, List<String> values);

}
