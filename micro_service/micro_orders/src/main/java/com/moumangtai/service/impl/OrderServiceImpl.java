package com.moumangtai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moumangtai.dao.OrderMapper;
import com.moumangtai.entity.Orders;
import com.moumangtai.service.IOrderService;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements IOrderService {
}
