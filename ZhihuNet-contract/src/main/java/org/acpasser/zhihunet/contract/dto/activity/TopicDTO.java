package org.acpasser.zhihunet.contract.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopicDTO implements Serializable {
    private Integer id;
    private Long topicId;
    private String name;
    private String topicType;
    private String url;
}
