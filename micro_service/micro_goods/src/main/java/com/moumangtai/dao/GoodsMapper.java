package com.moumangtai.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.moumangtai.entity.Goods;
import org.apache.ibatis.annotations.Param;

public interface GoodsMapper extends BaseMapper<Goods> {
    int updateGoodsSave(@Param("gid")Integer gid, @Param("gnumber")Integer gnumber);
}
