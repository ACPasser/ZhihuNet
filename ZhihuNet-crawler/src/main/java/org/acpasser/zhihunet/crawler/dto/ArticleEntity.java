package org.acpasser.zhihunet.crawler.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("article")
public class ArticleEntity extends BaseEntity {
    private String title;
    private Integer created;        // => article_created_time
    private Integer updated;        // => article_updated_time
    private Integer voteupCount;
    private String detail;
    private String excerpt;
    private String commentPermission;
}
