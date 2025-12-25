package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.model.ZhihuCollection;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuCollectionExtMapper {
    void batchUpsert(@Param("collections") List<ZhihuCollection> collections);
}