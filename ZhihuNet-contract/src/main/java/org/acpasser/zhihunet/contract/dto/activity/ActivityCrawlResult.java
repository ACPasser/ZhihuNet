package org.acpasser.zhihunet.contract.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityCrawlResult implements Serializable {

    /**
     * <Token, ActivityDTO>
     */
    private Map<String, ActivityDTO> activityDTOMap;

    /**
     * <Token, FailReason>
     */
    private Map<String, String> failedTokenMap;

}
