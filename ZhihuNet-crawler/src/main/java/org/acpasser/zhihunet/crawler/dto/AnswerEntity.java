package org.acpasser.zhihunet.crawler.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("answer")
public class AnswerEntity extends BaseEntity {
    private Integer voteupCount;
    private Integer thanksCount;
    private QuestionEntity question;
    private Integer createdTime;    // => answer_created_time
    private Integer updatedTime;    // => answer_updated_time
    private String commentPermission;
    private Boolean isCopyable;
    private CanCommentEntity canComment;      // => json字符串
    @Data
    public static class CanCommentEntity {
        private Boolean status;
        private String reason;
    }
    private String excerptNew;      // => excerpt
    private String answerType;
}
