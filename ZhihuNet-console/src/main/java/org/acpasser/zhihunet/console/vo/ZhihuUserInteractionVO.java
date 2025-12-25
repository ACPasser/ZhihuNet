package org.acpasser.zhihunet.console.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.acpasser.zhihunet.common.dto.zhihu.ZhihuUserInteractionDTO;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZhihuUserInteractionVO {
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate date;
    private List<ZhihuUserInteractionDTO> interactions;
}
