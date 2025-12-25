package org.acpasser.zhihunet.common.dto.zhihu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.model.ZhihuUser;
import org.springframework.beans.BeanUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZhihuUserDTO {
    private String token;
    private String name;
    private String userType;
    private String indexUrl;
    private String description;
    private String headline;
    private String ipInfo;
    private Byte gender;
    private Integer followerCount;
    private Integer followingCount;
    private Integer mutualFolloweesCount;
    private Integer answerCount;
    private Integer questionCount;
    private Integer articlesCount;
    private Integer columnsCount;
    private Integer zvideoCount;
    private Integer favoriteCount;
    private Integer favoritedCount;
    private Integer pinsCount;
    private Integer voteupCount;
    private Integer thankedCount;
    private Integer followingColumnsCount;
    private Integer followingTopicCount;
    private Integer followingQuestionCount;
    private String business;
    private String locations;
    private String employments;
    private String educations;

    public static ZhihuUserDTO convert(ZhihuUser user) {
        ZhihuUserDTO userDTO = new ZhihuUserDTO();
        BeanUtils.copyProperties(user, userDTO);
        return userDTO;
    }
}
