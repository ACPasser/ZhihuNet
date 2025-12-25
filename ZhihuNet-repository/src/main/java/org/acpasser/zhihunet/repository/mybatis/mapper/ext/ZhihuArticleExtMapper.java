package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.model.ZhihuArticle;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuArticleExtMapper {
    void batchUpsert(@Param("articles") List<ZhihuArticle> articles);
}