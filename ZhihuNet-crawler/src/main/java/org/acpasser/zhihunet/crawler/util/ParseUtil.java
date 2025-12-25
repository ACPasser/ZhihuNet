package org.acpasser.zhihunet.crawler.util;

import lombok.extern.slf4j.Slf4j;
import org.acpasser.zhihunet.common.exception.BusinessException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
public class ParseUtil {
    /**
     * 解析知乎【个人简介】：保留非链接内容，将 <a> 标签替换为解析后的链接
     * 示例：输入 "ABC<a href='...'>链接</a>DEF" → 输出 "ABChttps://xxx.comDEF"
     */
    public static String parseDesc(String desc) {
        if (desc == null || desc.isEmpty()) {
            return desc;
        }

        // 解析 HTML 片段
        Document doc = Jsoup.parseBodyFragment(desc);
        Element body = doc.body();

        // 遍历所有子节点，重组文本
        List<Node> nodes = body.childNodes();
        StringBuilder result = new StringBuilder();

        for (Node node : nodes) {
            if (node instanceof TextNode) {
                // 文本节点：直接拼接原始文本
                result.append(((TextNode) node).text());
            } else if (node instanceof Element) {
                Element element = (Element) node;
                if ("a".equals(element.tagName())) {
                    // a 标签：解析链接并替换
                    String href = element.attr("href");
                    String parsedUrl = parseUrlFromHref(href);
                    result.append(parsedUrl);
                } else {
                    // 其他标签（如 span、strong 等）：取其文本内容
                    result.append(element.text());
                }
            } else {
                // 非文本、非元素节点（如注释等）：忽略或拼接其字符串表示
                result.append(node.toString());
            }
        }

        return result.toString();
    }

    /**
     * 从 href 中提取链接（优先解析 target 参数，无则返回原始 href）
     */
    private static String parseUrlFromHref(String href) {
        if (href == null || href.isEmpty()) {
            return "";
        }

        // 尝试提取 target 参数（外部链接）
        String targetValue = null;
        String[] hrefParts = href.split("\\?", 2); // 分割基础 URL 和参数部分
        if (hrefParts.length >= 2) {
            String queryParams = hrefParts[1];
            String[] params = queryParams.split("&");
            for (String param : params) {
                if (param.startsWith("target=")) {
                    targetValue = param.substring("target=".length());
                    break;
                }
            }
        }

        // 处理链接：有 target 则解码，无则返回原始 href
        if (targetValue != null && !targetValue.isEmpty()) {
            try {
                return URLDecoder.decode(targetValue, StandardCharsets.UTF_8);
            } catch (Exception e) {
                return targetValue; // 解码失败返回原始值
            }
        } else {
            return href; // 内部链接直接返回 href
        }
    }

    public static <T> void batchSaveWithLog(String dataType, List<T> dataList, Consumer<List<T>> saveFunction) {
        if (!dataList.isEmpty()) {
            long startTime = System.currentTimeMillis();
            saveFunction.accept(dataList);
            long endTime = System.currentTimeMillis();
            log.info("数据[{}]自动入库完成，数量: {}，耗时: {}ms", dataType, dataList.size(), endTime - startTime);
        } else {
            log.info("数据[{}]为空，跳过保存", dataType);
        }
    }

    /**
     * 获取 resources 目录下文件的真实路径
     */
    private static String getResourcePath(String fileName) {
        try {
            // 通过类加载器获取资源URL
            URL resourceUrl = ParseUtil.class.getClassLoader().getResource(fileName);
            if (resourceUrl == null) {
                return null; // 文件不存在
            }
            // 转换为本地文件路径（处理特殊字符，如空格）
            return new File(resourceUrl.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            log.error("资源文件路径解析失败（URI 语法错误），文件名: {}", fileName, e);
        }
        return null;
    }

    public static String parseZse96(String apiUrl, String zse93, String dc0, int timeoutSeconds) {
        // Node.js 需要本地文件路径才能执行脚本（无法直接读取内存中的文件内容）
        String nodeJsPath = getResourcePath("x-zse-96.js");
        if (nodeJsPath == null) {
            throw new BusinessException("脚本文件不存在于 resources 目录下");
        }
        File nodeScript = new File(nodeJsPath);
        if (!nodeScript.exists() || !nodeScript.isFile()) {
            log.error("Node.js脚本文件不存在或不是文件: {}", nodeJsPath);
            throw new BusinessException(String.format("Node.js脚本文件不存在或不是文件: %s", nodeJsPath));
        }
        Process process = null;
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        try {
            // 构建进程命令（node + 脚本 + 参数）
            ProcessBuilder processBuilder = new ProcessBuilder("node", nodeJsPath, apiUrl, zse93, dc0);

            // 启动进程
            process = processBuilder.start();

            // 获取标准输出和错误输出流（指定UTF-8编码，避免乱码）
            reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            );
            errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream(), StandardCharsets.UTF_8)
            );

            // 读取错误输出（异步读取，避免脚本因缓冲区满而阻塞）
            StringBuilder errorMsg = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorMsg.append(errorLine).append("\n");
            }
            // 若有错误输出，记录警告（非致命错误可能仍有结果）
            if (!errorMsg.isEmpty()) {
                log.warn("Node.js脚本执行警告: {}", errorMsg.toString().trim());
            }

            // 读取脚本输出结果（x-zse-96）
            String zse96 = reader.readLine();

            // 等待进程执行完成（带超时）
            boolean exited = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
            if (!exited) {
                process.destroyForcibly();
                log.error("Node.js脚本执行超时（超时时间: {}秒）", timeoutSeconds);
                throw new BusinessException(String.format("Node.js脚本执行超时（超时时间: %s秒）", timeoutSeconds));
            }

            // 检查进程退出码（0表示成功，非0表示失败）
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("Node.js脚本执行失败（退出码: {}），错误信息: {}", exitCode, errorMsg);
                throw new BusinessException(String.format("Node.js脚本执行失败（退出码: %s），错误信息: %s",
                        exitCode, errorMsg));
            }

            // 检查结果是否有效
            if (zse96 == null || zse96.trim().isEmpty()) {
                log.error("Node.js脚本未返回有效x-zse-96结果");
                throw new BusinessException("Node.js脚本未返回有效x-zse-96结果");
            }

            return zse96;
        } catch (IOException e) {
            log.error("IO异常（如Node环境未安装、脚本无法执行）", e);
        } catch (InterruptedException e) {
            log.error("线程被中断", e);
            // 恢复中断状态，避免影响上层逻辑
            Thread.currentThread().interrupt();
        } catch (Exception e) { // 捕获其他未预期异常
            log.error("获取x-zse-96时发生未知异常", e);
        } finally {
            // 确保资源关闭（流和进程）
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("关闭输出流失败", e);
                }
            }
            if (errorReader != null) {
                try {
                    errorReader.close();
                } catch (IOException e) {
                    log.error("关闭错误流失败", e);
                }
            }
            if (process != null && process.isAlive()) {
                process.destroyForcibly(); // 强制销毁未结束的进程
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String apiUrl = "api/v3/moments/zi-you-zhi-yi-21-60/activities?limit=5&desktop=true";
        String zse93 = "101_3_3.0";
        String dc0 = "OqZT5s7uAxuPTmV6VcAsYmNaoo1DJN4cKgU=|1756900396";

        System.out.printf(Objects.requireNonNull(parseZse96(apiUrl, zse93, dc0, 5)));
    }
}