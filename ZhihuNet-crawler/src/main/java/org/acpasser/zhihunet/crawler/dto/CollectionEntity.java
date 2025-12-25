package org.acpasser.zhihunet.crawler.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("collection")
public class CollectionEntity extends BaseEntity {
    private Integer answerCount;    // 内容数
    private Integer followerCount;  // 关注数
    private Integer createdTime;
    private Integer updatedTime;
    private String title;
    private String description;
    private Boolean isPublic;       // 是否公开
    private UserEntity creator;     // 注：收藏夹不是author，而是creator
}
