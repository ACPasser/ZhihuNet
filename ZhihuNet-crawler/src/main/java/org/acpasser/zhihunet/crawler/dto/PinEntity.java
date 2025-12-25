package org.acpasser.zhihunet.crawler.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("pin")
public class PinEntity extends BaseEntity {
    private Integer likeCount;  // 赞同数
    private Integer favoriteCount;  // 收藏数
    private Integer repinCount;  // 转发/转载次数
    private String commentPermission;
    private String creationDisclaimer;
    private String excerptTitle;
    private Boolean adminClosedComment; // 是否被管理员关闭评论
    private Boolean isAdminCloseRepin;  // 是否由管理员关闭了转发/转载功能
    private Boolean isContainAiContent; // 是否包含 AI 生成内容
    private List<TopicEntity> topics;
    private Integer created;

    @Data
    public static class TopicEntity {
        private Long id;
        private String name;
        private String type;
        private String topicType;
        private String url;
    }
}
