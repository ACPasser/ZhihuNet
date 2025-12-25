package org.acpasser.zhihunet.contract.dto.user;

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
public class UserCrawlResult implements Serializable {

    /**
     * <Token, UserDTO>
     */
    private Map<String, UserDTO> userDTOMap;

    /**
     * <Token, FailReason>
     */
    private Map<String, String> failedTokenMap;

}
