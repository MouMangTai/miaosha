package com.moumangtai.controller;

import com.moumangtai.entity.Goods;
import com.moumangtai.entity.ResultData;
import com.moumangtai.service.IGoodsService;
import com.moumangtai.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/goods")
@CrossOrigin
public class GoodsController {

    @Autowired
    private IGoodsService iGoodsService;
    @RequestMapping("/times")
    public ResultData<List<String>> getSecKillTime(){

        List<String> times = new ArrayList<>();

        //计算5个场次的时间
        for (int i = 0; i < 5; i++) {
            String time = DateUtil.getSecKillTime(i);
            times.add(time);
        }
        return new ResultData<List<String>>().setData(times);
    }
    @RequestMapping("/listByTime")
    public ResultData<List<Goods>> getGoodsByTime(String time){
        List<Goods> data = iGoodsService.getSecGoodsListByTime(time);
        return new ResultData<List<Goods>>().setData(data);
    }
    @RequestMapping("/getGoodsById")
    public ResultData<Goods> getGoodsById(Integer gid){
        Goods goods = iGoodsService.getById(gid);
        return new ResultData<Goods>().setData(goods);
    }
    @RequestMapping("/now")
    public ResultData<Date> now(){
        String now = "2021-05-14 09:59:55";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return new ResultData<Date>().setData(simpleDateFormat.parse(now));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }


}
