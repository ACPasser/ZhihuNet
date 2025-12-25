package org.acpasser.zhihunet.repository.mybatis.repository;

import org.acpasser.zhihunet.model.ZhihuPin;
import org.acpasser.zhihunet.repository.mybatis.mapper.ZhihuPinMapper;
import org.acpasser.zhihunet.repository.mybatis.mapper.ext.ZhihuPinExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZhihuPinRepository {
    @Autowired
    private ZhihuPinMapper pinMapper;

    @Autowired
    private ZhihuPinExtMapper pinExtMapper;

    public void batchUpsert(List<ZhihuPin> pins) {
        if (pins == null || pins.isEmpty()) {
            return;
        }
        pinExtMapper.batchUpsert(pins);
    }
}
