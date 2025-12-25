package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.model.ZhihuAnswer;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuAnswerExtMapper {
    void batchUpsert(@Param("answers") List<ZhihuAnswer> answers);
}