package com.leyou.cart.service;

import com.leyou.cart.entity.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.threadlocals.UserHolder;
import com.leyou.common.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private String REFIX_KEY = "ly:cart:";

    public void addCart(Cart cart) {
        String skuId = cart.getSkuId().toString();
//        先判断此sku是否已存在购物车中
        String userId = UserHolder.getUserId();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REFIX_KEY + userId);
        addCartToRedis(cart, hashOps);
    }

    public List<Cart> findCartList() {
//        把当前登录人存储的购物车数据
        String userId = UserHolder.getUserId();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REFIX_KEY + userId);
        List<String> cartListJsonStr = hashOps.values();

        return cartListJsonStr.stream().map(cartJsonStr->{
            return JsonUtils.toBean(cartJsonStr,Cart.class);
        }).collect(Collectors.toList());

    }


    public void addCartList(List<Cart> cartList) {
        String userId = UserHolder.getUserId();
        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(REFIX_KEY + userId);
        for (Cart cart : cartList) {
            String skuId = cart.getSkuId().toString();
////        先判断此sku是否已存在购物车中
            addCartToRedis(cart, hashOps);
        }
    }
    private void addCartToRedis(Cart cart, BoundHashOperations<String, String, String> hashOps) {
        String skuId = cart.getSkuId().toString();
        if (hashOps.hasKey(skuId)) {  //redis中已经有此商品
//             数量累加
            String cartJsonString = hashOps.get(skuId);
            Cart cartRedis = JsonUtils.toBean(cartJsonString, Cart.class);
            cartRedis.setNum(cartRedis.getNum() + cart.getNum());
            hashOps.put(skuId, JsonUtils.toString(cartRedis));  //数量累加后再重新放入到redis中
        } else {
            hashOps.put(skuId, JsonUtils.toString(cart));
        }
    }


    public void updateNum(Long skuId, Integer num) {
        // 获取当前用户
        String userId = UserHolder.getUserId();
        String key = REFIX_KEY + userId;

        BoundHashOperations<String, String, String> hashOps = redisTemplate.boundHashOps(key);
        String hashKey = skuId.toString();
        Boolean aBoolean = hashOps.hasKey(hashKey);
        if (aBoolean==null || !aBoolean){
            log.error("购物车商品不存在，用户：{}, 商品：{}", userId, skuId);
            throw new LyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        // 查询购物车商品
        Cart cart = JsonUtils.toBean(hashOps.get(hashKey), Cart.class);
        // 修改数量
        cart.setNum(num);
        // 写回redis
        hashOps.put(hashKey,JsonUtils.toString(cart));
    }

    public void deleteCart(String skuId) {
        // 获取登录用户
        String key = REFIX_KEY + UserHolder.getUserId();
        this.redisTemplate.opsForHash().delete(key, skuId);
    }
}
