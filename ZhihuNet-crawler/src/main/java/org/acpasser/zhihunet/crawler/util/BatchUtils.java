package org.acpasser.zhihunet.crawler.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Slf4j
public class BatchUtils {
    /**
     * 从父列表中提取指定的子列表并合并（空值安全）
     * @param parentList 父列表
     * @param extractor 子列表提取器（比如 ActivityDTO::getInteractionDTOS）
     * @return 合并后的子列表
     */
    public static <T, R> List<R> mergeChildLists(Collection<T> parentList, Function<T, Collection<R>> extractor) {
        if (CollectionUtils.isEmpty(parentList)) {
            return Collections.emptyList();
        }
        return parentList.stream()
            .map(extractor)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .toList();
    }

    // 无需过滤
    public static <D, E> void convertAndBatchSave(
        List<D> dtoList,
        Supplier<E> entitySupplier,
        Consumer<List<E>> batchUpsertFunc,
        String targetType
    ) {
        convertAndBatchSave(dtoList, entitySupplier, batchUpsertFunc, targetType, null);
    }

    /**
     * 通用 DTO 转 Entity 并批量入库/更新（支持自定义过滤）
     * @param dtoList DTO列表
     * @param entitySupplier Entity实例创建器（无反射，如 ZhihuUserInteraction::new）
     * @param batchUpsertFunc 批量入库方法引用
     * @param targetType 目标类型（日志用）
     * @param filter 过滤规则（可选）：返回true保留，false过滤掉
     * @param <D> DTO类型
     * @param <E> Entity类型
     */
    public static <D, E> void convertAndBatchSave(
        List<D> dtoList,
        Supplier<E> entitySupplier,
        Consumer<List<E>> batchUpsertFunc,
        String targetType,
        Predicate<D> filter
    ) {
        if (CollectionUtils.isEmpty(dtoList)) {
            log.info("数据[{}]为空，跳过保存", targetType);
            return;
        }

        // 1. 执行过滤（无过滤则保留所有数据）
        Stream<D> dtoStream = dtoList.stream();
        if (filter != null) {
            dtoStream = dtoStream.filter(filter);
        }

        // 2. DTO转Entity
        List<E> entityList = dtoStream
            .map(dto -> {
                E entity = entitySupplier.get();
                BeanUtils.copyProperties(dto, entity);
                return entity;
            })
            .toList();

        // 3. 批量入库
        ParseUtil.batchSaveWithLog(targetType, entityList, batchUpsertFunc);
    }

    /**
     * 获取“过滤已存在ID”的Predicate
     * @param existIds 已存在的ID集合
     * @param idExtractor 从DTO提取ID的方法
     * @param <D> DTO类型
     * @param <ID> ID类型（Long/String等）
     * @return 过滤规则：仅保留ID不存在的DTO
     */
    public static <D, ID> Predicate<D> getNotExistIdFilter(Set<ID> existIds, Function<D, ID> idExtractor) {
        return dto -> {
            ID id = idExtractor.apply(dto);
            return id != null && !existIds.contains(id);
        };
    }
}
