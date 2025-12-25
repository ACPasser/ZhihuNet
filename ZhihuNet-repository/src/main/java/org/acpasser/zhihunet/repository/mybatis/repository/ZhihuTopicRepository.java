package org.acpasser.zhihunet.repository.mybatis.repository;

import org.acpasser.zhihunet.model.ZhihuTopic;
import org.acpasser.zhihunet.repository.mybatis.mapper.ZhihuTopicMapper;
import org.acpasser.zhihunet.repository.mybatis.mapper.ext.ZhihuTopicExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZhihuTopicRepository {
    @Autowired
    private ZhihuTopicMapper topicMapper;

    @Autowired
    private ZhihuTopicExtMapper topicExtMapper;

    public void batchUpsert(List<ZhihuTopic> topics) {
        if (topics == null || topics.isEmpty()) {
            return;
        }
        topicExtMapper.batchUpsert(topics);
    }
}
