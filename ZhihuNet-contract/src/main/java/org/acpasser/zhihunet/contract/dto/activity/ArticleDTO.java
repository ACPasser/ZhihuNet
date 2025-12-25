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
public class ArticleDTO implements Serializable {
    private Integer id;
    private Long articleId;
    private String url;
    private String title;
    private String excerpt;
    private String authorToken;
    private Date articleCreatedTime;
    private Date articleUpdatedTime;
    private Integer voteupCount;
    private Integer commentCount;
    private String commentPermission;
    private Date createdTime;
    private Date updatedTime;
    private Integer deletedAt;
}
