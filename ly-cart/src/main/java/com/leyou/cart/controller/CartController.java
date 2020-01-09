package com.leyou.cart.controller;

import com.leyou.cart.entity.Cart;
import com.leyou.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加购物车
     *
     * @param cart
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart) {
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT.value()).build();
    }

    /**
     * 查询购物车列表
     *
     * @return
     */
    @GetMapping("/list")
    public ResponseEntity<List<Cart>> findCartList() {
        List<Cart> carts = this.cartService.findCartList();
        if (carts == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(carts);
    }

    /**
     * 把localStorage的数据合并到redis中
     * @param cartList
     * @return
     */
    @PostMapping(value = "/list",name = "把localStorage的数据合并到redis中")
    public ResponseEntity<List<Cart>> addCartList(@RequestBody List<Cart> cartList){
        cartService.addCartList(cartList);
        return ResponseEntity.ok().build();
    }
    /**
     * 修改购物车中的商品数量
     *
     * @param skuId
     * @param num   商品数量
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateNum(
            @RequestParam("id") Long skuId,
            @RequestParam("num") Integer num) {
        this.cartService.updateNum(skuId, num);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除购物车中的商品
     * @param skuId
     * @return
     */
    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId) {
        this.cartService.deleteCart(skuId);
        return ResponseEntity.ok().build();
    }
}
