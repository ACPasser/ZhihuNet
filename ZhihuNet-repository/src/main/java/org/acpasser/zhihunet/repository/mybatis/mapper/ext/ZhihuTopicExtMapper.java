package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.model.ZhihuTopic;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuTopicExtMapper {
    void batchUpsert(@Param("topics") List<ZhihuTopic> topics);
}