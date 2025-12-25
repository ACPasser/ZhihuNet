package org.acpasser.zhihunet.repository.mybatis.repository;

import org.acpasser.zhihunet.model.ZhihuQuestion;
import org.acpasser.zhihunet.repository.mybatis.mapper.ZhihuQuestionMapper;
import org.acpasser.zhihunet.repository.mybatis.mapper.ext.ZhihuQuestionExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZhihuQuestionRepository {
    @Autowired
    private ZhihuQuestionMapper questionMapper;

    @Autowired
    private ZhihuQuestionExtMapper questionExtMapper;

    public void batchUpsert(List<ZhihuQuestion> questions) {
        if (questions == null || questions.isEmpty()) {
            return;
        }
        questionExtMapper.batchUpsert(questions);
    }
}
