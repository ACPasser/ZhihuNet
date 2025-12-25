package org.acpasser.zhihunet.crawler.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.acpasser.zhihunet.crawler.enums.TargetType;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = QuestionEntity.class, name = "question"),
        @JsonSubTypes.Type(value = AnswerEntity.class, name = "answer"),
        @JsonSubTypes.Type(value = ArticleEntity.class, name = "article"),
        @JsonSubTypes.Type(value = PinEntity.class, name = "pin"),
        @JsonSubTypes.Type(value = CollectionEntity.class, name = "collection")
})
@Data
public abstract class BaseEntity {
    protected Long id;
    @JsonProperty("type")
    protected TargetType type;
    protected String url;
    protected Integer commentCount;
    private UserEntity author;

    @Data
    public static class UserEntity {
        private String id;
        private String name;
        private String headline;
        private String type;
        private String userType;
        private String url;
        private String urlToken;        // => actor_token or author_token，为空则表示匿名用户
        private String avatarUrl;
        private Boolean gender;
        private Boolean isFollowing;    // 关注（作者）
        private Boolean isFollowed;     // 被关注（作者）
        private Boolean isOrg;
    }

    public boolean isQuestion() {
        return TargetType.QUESTION.equals(type);
    }

    public boolean isAnswer() {
        return TargetType.ANSWER.equals(type);
    }

    public boolean isArticle() {
        return TargetType.ARTICLE.equals(type);
    }

    public boolean isPin() {
        return TargetType.PIN.equals(type);
    }

    public boolean isCollection() {
        return TargetType.COLLECTION.equals(type);
    }
}
