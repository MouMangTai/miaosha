package com.moumangtai.util;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.AccessType;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class TokenTong {
    //当前令牌桶的key
    private String key;
    //令牌桶的最大容量
    private int maxTokens;
    //每秒产生的令牌的数量
    private int secTokens;


    private StringRedisTemplate redisTemplatel;

    private String initTokenLua = "--判断key是否存在，如果不存在就初始化令牌桶\n" +
            "redis.replicate_commands()\n"+
            "--获得参数key并且用..进行拼接\n" +
            "local key = 'tongKey_'..KEYS[1]\n" +
            "\n" +
            "--令牌桶的最大容量\n" +
            "local maxTokens = tonumber(ARGV[1])\n" +
            "\n" +
            "--每秒产生的令牌数量\n" +
            "local secTokens = tonumber(ARGV[2])\n" +
            "\n" +
            "--计算当前时间（微秒）\n" +
            "local nextTime = tonumber(ARGV[3])\n" +
            "\n" +
            "\n" +
            "--判断令牌桶是否存在\n" +
            "local result = redis.call('exists',key)\n" +
            "if result == 0 then\n" +
            "redis.call('hmset',key,'hasTokens',maxTokens,'maxTokens',maxTokens,'secTokens',secTokens,'nextTime',nextTime)\n" +
            "end";


    private String tokenLua = "--当前领取的令牌桶的key\n" +
            "redis.replicate_commands()\n"+
            "local key = 'tongKey_'..KEYS[1]\n" +
            "\n" +
            "--获取当前需要领取令牌的数量\n" +
            "local getTokens = tonumber(ARGV[1])\n" +
            "\n" +
            "--获取令牌桶中的参数\n" +
            "local hasTokens = tonumber(redis.call('hget',key,'hasTokens'))\n" +
            "\n" +
            "--获得最大的令牌数\n" +
            "local maxTokens= tonumber(redis.call('hget',key,'maxTokens'))\n" +
            "\n" +
            "\n" +
            "--每秒生产的令牌的数量\n" +
            "local secTokens= tonumber(redis.call('hget',key,'secTokens'))\n" +
            "\n" +
            "--下一次可以生产令牌的时间（微妙）\n" +
            "local nextTime = tonumber(redis.call('hget',key,'nextTime'))\n" +
            "\n" +
            "\n" +
            "--当前时间（微妙值）\n" +
            "local nowArray = redis.call('time')\n" +
            "local nowTime = nowArray[1]*1000000 + nowArray[2]\n" +
            "\n" +
            "--单个令牌生成的耗时\n" +
            "local singTokenTime = 1000000/secTokens\n" +
            "\n" +
            "\n" +
            "--获得超时时间\n" +
            "local timeout = tonumber(ARGV[2] or -1)\n" +
            "\n" +
            "--判断超时时间\n" +
            "if timeout ~= -1 then\n" +
            "    if timeout < nextTime - nowTime then\n" +
            "        return -1\n" +
            "    end\n" +
            "end\n" +
            "\n" +
            "\n" +
            "\n" +
            "--重新计算令牌\n" +
            "if nowTime > nextTime then\n" +
            "    --计算上一次生成令牌到现在的差时\n" +
            "    local hasTime = nowTime - nextTime\n" +
            "    --可以产生的令牌数\n" +
            "    local createTokens = hasTime/singTokenTime\n" +
            "    --当前总的令牌数\n" +
            "    hasTokens = math.min(hasTokens+createTokens,maxTokens)\n" +
            "    --重新设置下一次可以生成令牌的时间\n" +
            "    nextTime = nowTime\n" +
            "end\n" +
            "\n" +
            "\n" +
            "--获取令牌\n" +
            "\n" +
            "--计算当前能够拿走的令牌\n" +
            "local canGetTokens = math.min(hasTokens,getTokens)\n" +
            "--计算需要预支的令牌数量\n" +
            "local yuzhiTokens = getTokens - canGetTokens\n" +
            "--计算如果预支这些令牌，需要多少时间（微秒）\n" +
            "local yuzhiTime = yuzhiTokens * singTokenTime\n" +
            "--重新设置令牌桶中的值\n" +
            "hasTokens = hasTokens - canGetTokens\n" +
            "\n" +
            "\n" +
            "--更新令牌桶\n" +
            "redis.call('hmset',key,'hasTokens',hasTokens,'nextTime',nextTime+yuzhiTime)\n" +
            "\n" +
            "--返回当前请求需要等待的时间\n" +
            "return nextTime -nowTime";



    public TokenTong(String key,int maxTokens,int secTokens){
        this.key = key;
        this.maxTokens = maxTokens;
        this.secTokens = secTokens;

        this.redisTemplatel = SpringUtil.getBean(StringRedisTemplate.class);
        //手动从spring容器中获取StringRedisTemplate对象

        init();
    }
    //初始化令牌桶到redis
    private void init(){
//        if(!redisTemplatel.hasKey(key)){
//            redisTemplatel.opsForHash().put(key,"hasTokens",maxTokens);
//            redisTemplatel.opsForHash().put(key,"maxTokens",maxTokens);
//            redisTemplatel.opsForHash().put(key,"secTokens",secTokens);
//            //微秒
//            redisTemplatel.opsForHash().put(key,"nextTime", TimeUnit.MICROSECONDS.toMillis(System.currentTimeMillis()));
//        }


        redisTemplatel.execute(
                new DefaultRedisScript(initTokenLua),
                Collections.singletonList(key+""),
                maxTokens+"",secTokens+"",TimeUnit.MILLISECONDS.toMicros(System.currentTimeMillis())+"");

    }

    //1.传入领取的令牌数，返回需要等待的时间
    public double getTokens(int tokens){
        long waitTime = redisTemplatel.execute(
                new DefaultRedisScript<Long>(tokenLua,Long.class),
                Collections.singletonList(key+""),
                tokens+"");
        if(waitTime>0){
            try {
                Thread.sleep(waitTime/1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return waitTime;
    }
    //2.传入领取的令牌数，超时时间，如果在超时时间之内，没办法领取令牌，就返回错误
    public Boolean getTokens(int tokens,int timeout,TimeUnit unit){
        long waitTime = redisTemplatel.execute(
                new DefaultRedisScript<Long>(tokenLua,Long.class),
                Collections.singletonList(key+""),
                tokens+"",
                unit.toMicros(timeout)+"");
        if(waitTime == -1){
            return false;
        }else if(waitTime>0){
            try {
                Thread.sleep(waitTime/1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }
    //3.传入领取的令牌数，如果能够立刻获取，就返回true，如果需要等待，就立即返回false
    public Boolean getTokensNow(int tokens){
        return getTokens(tokens,0,TimeUnit.MICROSECONDS);
    }

}
