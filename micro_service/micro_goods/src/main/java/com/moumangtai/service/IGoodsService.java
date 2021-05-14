package com.moumangtai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.moumangtai.entity.Goods;

import java.util.List;

public interface IGoodsService extends IService<Goods> {

    List<Goods> getSecGoodsListByTime(String time);

    int updateGoodsSave(Integer gid,Integer gnumber);
}
