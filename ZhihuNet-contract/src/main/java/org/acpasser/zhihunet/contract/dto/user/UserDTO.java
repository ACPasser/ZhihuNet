package org.acpasser.zhihunet.contract.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
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
}
