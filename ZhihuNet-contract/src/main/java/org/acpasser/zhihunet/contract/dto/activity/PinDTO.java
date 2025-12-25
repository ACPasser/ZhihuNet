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
public class PinDTO implements Serializable {
    private Integer id;
    private Long pinId;
    private String authorToken;
    private Integer commentCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer repinCount;
    private String creationDisclaimer;
    private String commentPermission;
    private String url;
    private Date pinCreatedTime;
    private String topicIds;
    private Boolean adminClosedComment;
    private Boolean adminCloseRepin;
    private Boolean containAiContent;
    private Date createdTime;
    private Date updatedTime;
    private Integer deletedAt;
    private String excerptTitle;
}
