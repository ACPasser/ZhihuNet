package org.acpasser.zhihunet.repository.mybatis.repository;

import org.acpasser.zhihunet.common.dto.ListZhihuUserCondition;
import org.acpasser.zhihunet.model.ZhihuUser;
import org.acpasser.zhihunet.model.ZhihuUserExample;
import org.acpasser.zhihunet.repository.mybatis.mapper.ZhihuUserMapper;
import org.acpasser.zhihunet.repository.mybatis.mapper.ext.ZhihuUserExtMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

@Repository
public class ZhihuUserRepository {
    @Autowired
    private ZhihuUserMapper zhihuUserMapper;

    @Autowired
    private ZhihuUserExtMapper zhihuUserExtMapper;

    public List<ZhihuUser> list(ListZhihuUserCondition condition) {
        return zhihuUserExtMapper.list(condition);
    }

    public List<ZhihuUser> listAll() {
        ZhihuUserExample example = new ZhihuUserExample();
        example.createCriteria().andDeletedAtEqualTo(0);
        return zhihuUserMapper.selectByExample(example);
    }

    public long count(ListZhihuUserCondition condition) {
        return zhihuUserExtMapper.count(condition);
    }

    public ZhihuUser getByToken(String token) {
        ZhihuUserExample example = new ZhihuUserExample();
        example.createCriteria().andTokenEqualTo(token).andDeletedAtEqualTo(0);
        List<ZhihuUser> zhihuUsers = zhihuUserMapper.selectByExample(example);
        if (zhihuUsers.isEmpty()) {
            return null;
        }
        return zhihuUsers.getFirst();
    }

    // 清理注销用户
    public int deleteNonExistUsers() {
        ZhihuUserExample example = new ZhihuUserExample();

        example.createCriteria()
            .andDeletedAtEqualTo(0)
            .andNameEqualTo("")
            .andIndexUrlEqualTo("");

        ZhihuUser updateRecord = new ZhihuUser();
        updateRecord.setDeletedAt((int) (System.currentTimeMillis() / 1000));
        updateRecord.setUpdatedTime(new Date());
        return zhihuUserMapper.updateByExampleSelective(updateRecord, example);
    }

    public void batchUpsert(List<ZhihuUser> zhihuUsers) {
        if (CollectionUtils.isEmpty(zhihuUsers)) {
            return;
        }
        zhihuUserExtMapper.batchUpsert(zhihuUsers);
    }
}
