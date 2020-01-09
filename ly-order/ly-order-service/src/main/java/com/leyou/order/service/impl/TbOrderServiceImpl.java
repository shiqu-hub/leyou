package com.leyou.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.order.dto.CartDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.entity.TbOrder;
import com.leyou.order.entity.TbOrderDetail;
import com.leyou.order.entity.TbOrderLogistics;
import com.leyou.order.enums.BusinessTypeEnum;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.mapper.TbOrderMapper;
import com.leyou.order.service.TbOrderDetailService;
import com.leyou.order.service.TbOrderLogisticsService;
import com.leyou.order.service.TbOrderService;
import com.leyou.order.vo.OrderVO;
import com.leyou.user.client.UserClient;
import com.leyou.user.dto.UserAddressDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author syl
 * @since 2019-12-25
 */
@Service
public class TbOrderServiceImpl extends ServiceImpl<TbOrderMapper, TbOrder> implements TbOrderService {


    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ItemClient itemClient;
    @Autowired
    private UserClient userClient;
    @Autowired
    private TbOrderDetailService orderDetailService;
    @Autowired
    private TbOrderLogisticsService orderLogisticsService;


    @Override
    @Transactional
    public Long createOrder(OrderDTO order) {
        // 创建order对象
        TbOrder order1 = new TbOrder();
        // 订单编号
        long orderId = idWorker.nextId();
        order1.setOrderId(orderId);
        // 登录用户
        String userId = UserHolder.getUserId();
        order1.setUserId(Long.parseLong(userId));
        order1.setSourceType(2); // '订单来源：1:app端，2：pc端，3：微信端',
        // 设置金额
        List<CartDTO> carts = order.getCarts();
        // 获取所有skuId
        List<Long> skuIds = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        // 处理CartDTO为一个map， 其key是skuId；值是num
        Map<Long, Integer> numMap = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        // 查询sku
        List<SkuDTO> skuDTOList = itemClient.querySkuByIds(skuIds);
        // 定义一个OrderDetail的集合
        ArrayList<TbOrderDetail> details = new ArrayList<>();
        // 计算总金额
        long total = 0;
        for (SkuDTO skuDTO : skuDTOList) {
            Integer num = numMap.get(skuDTO.getId());
            total += skuDTO.getPrice() * num;
            // 组装OrderDetail
            TbOrderDetail detail = new TbOrderDetail();
            detail.setOrderId(orderId);
            detail.setImage(StringUtils.substringBefore(skuDTO.getImages(), ","));
            detail.setNum(num);
            detail.setSkuId(skuDTO.getId());
            detail.setOwnSpec(skuDTO.getOwnSpec());
            detail.setPrice(skuDTO.getPrice());
            detail.setTitle(skuDTO.getTitle());
            details.add(detail);
        }
        // 填写金额数据
        order1.setTotalFee(total);
        // 设置业务类型
        order1.setBType(BusinessTypeEnum.MALL.value());
        order1.setPaymentType(order.getPaymentType());
        order1.setPostFee(0L); //'邮费。
        order1.setActualFee(total + order1.getPostFee()/* - 优惠金额*/);
        // 订单状态初始化
        order1.setStatus(OrderStatusEnum.INIT.value());
        // 将order1写入数据库
        boolean b = this.save(order1);
        if (!b) {
            log.error("【新增订单】保存order表错误");
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        // 将details写入数据库
        boolean bDetail = orderDetailService.saveBatch(details);
        if (!bDetail) {
            log.error("【新增订单】保存orderDetail表错误");
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        // 写orderLogistics
        Long addressId = order.getAddressId();
        UserAddressDTO addr = userClient.queryAddressById(addressId);
        // 填写物流信息
        TbOrderLogistics tbOrderLogistics = BeanHelper.copyProperties(addr, TbOrderLogistics.class);
        tbOrderLogistics.setOrderId(orderId);

        boolean b1 = orderLogisticsService.save(tbOrderLogistics);
        if (!b1) {
            log.error("【新增订单】保存orderLogistics物流信息表错误");
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        // 减库存
        userClient.minusStock(numMap);

        return orderId;
    }

    @Override
    public OrderVO findOrderById(Long orderId) {
        return null;
    }
}
