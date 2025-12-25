package org.acpasser.zhihunet.repository.mybatis.repository;

import org.acpasser.zhihunet.model.ZhihuCollection;
import org.acpasser.zhihunet.repository.mybatis.mapper.ZhihuCollectionMapper;
import org.acpasser.zhihunet.repository.mybatis.mapper.ext.ZhihuCollectionExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZhihuCollectionRepository {
    @Autowired
    private ZhihuCollectionMapper collectionMapper;

    @Autowired
    private ZhihuCollectionExtMapper collectionExtMapper;


    public void batchUpsert(List<ZhihuCollection> collections) {
        if (collections == null || collections.isEmpty()) {
            return;
        }
        collectionExtMapper.batchUpsert(collections);
    }
}
