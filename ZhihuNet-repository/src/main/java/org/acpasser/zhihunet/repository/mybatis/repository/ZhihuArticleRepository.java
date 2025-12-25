package org.acpasser.zhihunet.repository.mybatis.repository;

import org.acpasser.zhihunet.model.ZhihuArticle;
import org.acpasser.zhihunet.repository.mybatis.mapper.ZhihuArticleMapper;
import org.acpasser.zhihunet.repository.mybatis.mapper.ext.ZhihuArticleExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZhihuArticleRepository {
    @Autowired
    private ZhihuArticleMapper articleMapper;

    @Autowired
    private ZhihuArticleExtMapper articleExtMapper;

    public void batchUpsert(List<ZhihuArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }
        articleExtMapper.batchUpsert(articles);
    }
}
