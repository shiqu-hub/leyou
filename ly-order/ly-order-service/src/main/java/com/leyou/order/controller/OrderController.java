package com.leyou.order.controller;


import com.leyou.order.dto.OrderDTO;
import com.leyou.order.service.TbOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private TbOrderService orderService;

    public ResponseEntity<Long> createOrder(@Valid OrderDTO orderDTO){
      Long orderId = this.orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(orderId);
    }
}
