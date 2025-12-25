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
public class InteractionDTO implements Serializable {
    private Integer id;
    private Long interactionId;
    private String actorToken;
    private String authorToken;
    private Boolean isFollowing;
    private Boolean isFollowed;
    private String type;
    private String actionText;
    private Long targetId;
    private String targetType;
    private String verb;
    private Date interactionTime;
    private Date createdTime;
    private Date updatedTime;
    private Integer deletedAt;
}
