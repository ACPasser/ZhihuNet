package org.acpasser.zhihunet.contract.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO implements Serializable {
    private Integer id;
    private Long questionId;
    private String questionType;
    private String title;
    private String url;
    private String authorToken;
    private Date questionCreatedTime;
    private Integer answerCount;
    private Integer commentCount;
    private Integer followerCount;
    private String excerpt;
    private String boundTopicIds;
    private Date createdTime;
    private Date updatedTime;
    private Integer deletedAt;
    private String detail;
}
