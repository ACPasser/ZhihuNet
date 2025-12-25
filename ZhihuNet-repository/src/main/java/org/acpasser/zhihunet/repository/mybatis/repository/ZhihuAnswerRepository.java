package org.acpasser.zhihunet.repository.mybatis.repository;

import org.acpasser.zhihunet.model.ZhihuAnswer;
import org.acpasser.zhihunet.repository.mybatis.mapper.ZhihuAnswerMapper;
import org.acpasser.zhihunet.repository.mybatis.mapper.ext.ZhihuAnswerExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ZhihuAnswerRepository {
    @Autowired
    private ZhihuAnswerMapper answerMapper;

    @Autowired
    private ZhihuAnswerExtMapper answerExtMapper;

    public void batchUpsert(List<ZhihuAnswer> answers) {
        if (answers == null || answers.isEmpty()) {
            return;
        }
        answerExtMapper.batchUpsert(answers);
    }
}
