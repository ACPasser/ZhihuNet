package org.acpasser.zhihunet.crawler.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ZhihuRequest {
    private String url; // 请求URL
    private Map<String, String> headers = new HashMap<>(); // 动态请求头
    private Map<String, String> params; // 请求参数（可选）
}
