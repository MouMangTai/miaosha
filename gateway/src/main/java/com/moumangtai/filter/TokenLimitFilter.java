package com.moumangtai.filter;

import com.alibaba.fastjson.JSON;
import com.moumangtai.entity.ResultData;
import com.moumangtai.util.TokenTong;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class TokenLimitFilter implements GatewayFilter {

    /**
     * key - 请求的url
     * value - 当前url对应的令牌桶
     */
    private Map<String, TokenTong> tongMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        //令牌桶限流 -URL
        //获取当前请求的URL
        ServerHttpRequest request = exchange.getRequest();
        String requestPath = request.getPath().value();
        //如果map中，key不存在，则调用第二个参数的方法初始化一个值，放入当前的key中，如果存在直接返回
        TokenTong tokenTong = tongMap.computeIfAbsent(requestPath,s -> new TokenTong(s,1000,1000));

        //领取令牌
        boolean flag = tokenTong.getTokensNow(2);

        if (flag){
            System.out.println("申请到令牌放行");
            //请求放行
            return chain.filter(exchange);
        }
        System.out.println("未申请到令牌,当前服务器繁忙");
        //没有则返回服务器繁忙
        ResultData resultData = new ResultData().setCode(ResultData.Code.ERROR).setMsg("服务器繁忙,请稍后再试");
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().put("Content-Type", Collections.singletonList("application/json;charset=utf-8"));
        //解决跨域问题
        response.getHeaders().put("Access-Control-Allow-Origin", Collections.singletonList("*"));

        DataBuffer dataBuffer = null;
        try {
            dataBuffer = response.bufferFactory().wrap(JSON.toJSONString(resultData).getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Mono<Void> voidMono = response.writeWith(Mono.just(dataBuffer));
        return voidMono;
    }
}
