package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.model.ZhihuUserInteraction;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuUserInteractionExtMapper {
    void batchUpsert(@Param("interactions") List<ZhihuUserInteraction> interactions);
}