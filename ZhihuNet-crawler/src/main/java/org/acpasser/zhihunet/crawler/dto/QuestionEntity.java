package org.acpasser.zhihunet.crawler.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("question")
public class QuestionEntity extends BaseEntity {
    private String title;
    private String questionType;
    private Integer created;        // => question_created_time
    private Integer answerCount;
    private Integer followerCount;
    private String detail;
    private String excerpt;
    private List<Integer> boundTopicIds;    // => 逗号分隔字符串
}
