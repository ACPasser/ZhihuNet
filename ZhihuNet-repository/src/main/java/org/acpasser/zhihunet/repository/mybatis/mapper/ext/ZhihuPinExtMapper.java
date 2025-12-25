package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.model.ZhihuPin;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuPinExtMapper {
    void batchUpsert(@Param("pins") List<ZhihuPin> pins);
}