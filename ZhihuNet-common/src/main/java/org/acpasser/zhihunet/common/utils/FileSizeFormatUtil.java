package org.acpasser.zhihunet.common.utils;

public class FileSizeFormatUtil {
    // 单位换算常量，这里参照操作系统显示的文件大小
    private static final long KB = 1000;
    private static final long MB = KB * 1000;
    private static final long GB = MB * 1000;

    /**
     * 将字节数转换为易读格式（K、M、G）
     * @param bytes 文件大小（字节）
     * @return 格式化后的字符串（如：1.2KB、3.5MB）
     */
    public static String format(long bytes) {
        if (bytes < 0) {
            return "无效大小";
        } else if (bytes < KB) {
            return bytes + "B";
        } else if (bytes < MB) {
            return String.format("%.1fKB", bytes / (double) KB);
        } else if (bytes < GB) {
            return String.format("%.1fMB", bytes / (double) MB);
        } else {
            return String.format("%.1fGB", bytes / (double) GB);
        }
    }
}
