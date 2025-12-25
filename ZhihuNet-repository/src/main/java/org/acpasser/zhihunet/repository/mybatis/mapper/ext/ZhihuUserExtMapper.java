package org.acpasser.zhihunet.repository.mybatis.mapper.ext;

import org.acpasser.zhihunet.common.dto.ListZhihuUserCondition;
import org.acpasser.zhihunet.model.ZhihuUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhihuUserExtMapper {

    List<ZhihuUser> list(ListZhihuUserCondition condition);

    long count(ListZhihuUserCondition condition);

    void batchUpsert(@Param("zhihuUsers") List<ZhihuUser> zhihuUsers);
}
