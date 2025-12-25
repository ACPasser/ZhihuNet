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
public class AnswerDTO implements Serializable {
    private Integer id;
    private Long answerId;
    private String answerType;
    private String authorToken;
    private String url;
    private Integer voteupCount;
    private Integer thanksCount;
    private Date answerCreatedTime;
    private Date answerUpdatedTime;
    private String commentPermission;
    private Boolean isCopyable;
    private Integer commentCount;
    private String canComment;
    private String excerpt;
    private Long questionId;
    private Date createdTime;
    private Date updatedTime;
    private Integer deletedAt;
}
