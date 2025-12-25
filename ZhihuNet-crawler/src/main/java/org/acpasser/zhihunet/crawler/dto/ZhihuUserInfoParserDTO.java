package org.acpasser.zhihunet.crawler.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZhihuUserInfoParserDTO {
    private String id;
    private String urlToken;
    private String name;
    private String userType;
    private String headline;
    private Boolean isActive;   // 知乎字段，暂不清楚有何作用（是否活跃？），先不使用。
    private String description;
    private Byte gender;
    private Boolean isAdvertiser;
    private String ipInfo;
    private Integer followerCount;
    private Integer followingCount;
    private Integer mutualFolloweesCount;
    private Integer answerCount;
    private Integer questionCount;
    private Integer articlesCount;
    private Integer columnsCount;
    private Integer zvideoCount;
    private Integer favoriteCount;
    private Integer favoritedCount;
    private Integer pinsCount;
    private Integer voteupCount;
    private Integer thankedCount;
    private Integer followingColumnsCount;
    private Integer followingTopicCount;
    private Integer followingQuestionCount;
    private Entity business;
    private List<Entity> locations;
        private List<Employment> employments;

    // 专用类：对应 employments 数组中的单个元素
    @Data
    public static class Employment {
        private Entity job;      // 职位（对应原始数据中的 job）
        private Entity company;  // 公司（对应原始数据中的 company）
    }

    private List<Education> educations;

    // 专用类：对应 educations 数组中的单个元素
    @Data
    public static class Education {
        private Entity school;   // 学校（对应原始数据中的 school）
    }

    // 知乎实体内部的公共结构
    @Data
    public static class Entity {
        private String id;
        private String type;
        private String url;
        private String name; // 需要的 name 字段
        private String avatarUrl;
    }

    public String getFormattedEmployments() {
        List<String> employmentList = new ArrayList<>();
        if (employments == null) {
            return "";
        }

        for (Employment emp : employments) {
            if (emp == null) continue;

            // 提取公司和职位名称
            String companyName = emp.getCompany() != null ? emp.getCompany().getName() : null;
            String jobName = emp.getJob() != null ? emp.getJob().getName() : null;

            // 拼接规则：公司-职位
            String employmentStr = buildEmploymentStr(companyName, jobName);
            if (employmentStr != null) {
                employmentList.add(employmentStr);
            }
        }

        return String.join(",", employmentList);
    }

    /**
     * 提取并格式化教育经历：拼接学校名称，多个用逗号分隔
     * @return 格式化后的教育经历字符串（如 "复旦大学,清华大学"）
     */
    public String getFormattedEducations() {
        List<String> educationList = new ArrayList<>();
        if (educations == null) {
            return ""; // 或返回 null，根据业务需求调整
        }

        for (Education edu : educations) {
            if (edu == null || edu.getSchool() == null) continue;

            String schoolName = edu.getSchool().getName();
            if (schoolName != null) {
                educationList.add(schoolName);
            }
        }

        return String.join(",", educationList);
    }

    /**
     * 辅助方法：拼接单个工作经历（公司-职位）
     */
    private String buildEmploymentStr(String companyName, String jobName) {
        if (companyName != null && jobName != null) {
            return companyName + "-" + jobName;
        } else if (companyName != null) {
            return companyName;
        } else return jobName;
    }
}
