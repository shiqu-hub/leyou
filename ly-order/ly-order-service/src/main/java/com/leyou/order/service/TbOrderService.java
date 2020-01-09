package com.leyou.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.TbOrder;
import com.leyou.order.vo.OrderVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author syl
 * @since 2019-12-25
 */
public interface TbOrderService extends IService<TbOrder> {

    Long createOrder(OrderDTO order);

    OrderVO findOrderById(Long orderId);
}
