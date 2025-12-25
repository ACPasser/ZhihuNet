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
public class CollectionDTO implements Serializable {
    private Integer id;
    private Long collectionId;
    private String creatorToken;
    private String title;
    private String description;
    private Date collectionCreatedTime;
    private Date collectionUpdatedTime;
    private Boolean publicStatus;
    private Integer answerCount;
    private Integer followerCount;
    private Integer commentCount;
    private String url;
    private Date createdTime;
    private Date updatedTime;
    private Integer deletedAt;
}
