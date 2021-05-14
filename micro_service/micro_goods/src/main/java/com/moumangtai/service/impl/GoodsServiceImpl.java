package com.moumangtai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moumangtai.dao.GoodsMapper;
import com.moumangtai.entity.Goods;
import com.moumangtai.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper,Goods> implements IGoodsService {
    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Cacheable(cacheNames = "seckill",key = "'times'+#time")
    public List<Goods> getSecGoodsListByTime(String time) {
        System.out.println("查询数据库了");
        QueryWrapper<Goods>queryWrapper = new QueryWrapper<Goods>().eq("begin_time",time);
        List<Goods> goods = goodsMapper.selectList(queryWrapper);
        return goods;
    }

    @Override
    public int updateGoodsSave(Integer gid, Integer gnumber) {
        System.out.println("到达Service层");
        return goodsMapper.updateGoodsSave(gid,gnumber);
    }

    @Override
    @Cacheable(cacheNames = "seckill",key = "'goods'+#id")
    public Goods getById(Serializable id) {
        System.out.println("查询数据库了");
        return super.getById(id);
    }
}
