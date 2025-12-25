package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.model.ZhihuQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuQuestionExtMapper {
    void batchUpsert(@Param("questions") List<ZhihuQuestion> questions);
}