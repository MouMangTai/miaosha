package com.moumangtai.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {

    public static String getSecKillTime(int i){
        //根据当前时间 计算第一场的时间
        Calendar calendar = Calendar.getInstance();
        //获得当前的小时
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        if(h % 2 != 0){
            h = h - 1;
        }

        calendar.set(Calendar.HOUR_OF_DAY,h);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

        //计算场次
        calendar.add(Calendar.HOUR_OF_DAY,i*2);

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(calendar.getTime());

    }
}
