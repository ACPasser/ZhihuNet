package org.acpasser.zhihunet.common.dto.zhihu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.model.ZhihuUserInteraction;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZhihuUserInteractionDTO {
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

    public static ZhihuUserInteractionDTO convert(ZhihuUserInteraction userInteraction) {
        ZhihuUserInteractionDTO userInteractionDTO = new ZhihuUserInteractionDTO();
        BeanUtils.copyProperties(userInteraction, userInteractionDTO);
        return userInteractionDTO;
    }
}
