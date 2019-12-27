package com.leyou.item.controller;


import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    /**
     * @param page     当前页
     * @param rows     每页显示个数
     * @param key      查询条件
     * @param saleable 是否上下架
     * @return
     */
    @GetMapping(value = "/spu/page", name = "分页查询商品SPU信息")
    public ResponseEntity<PageResult<SpuDTO>> findSpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable) {
        PageResult<SpuDTO> spuPage = goodsService.findSpuByPage(page, rows, key, saleable);
        return ResponseEntity.ok(spuPage);
    }

    @PostMapping(value = "/goods", name = "新增商品信息")
    public ResponseEntity<Void> saveGoods(@RequestBody SpuDTO spuDTO) {
        goodsService.saveGoods(spuDTO);
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/spu/saleable", name = "上下架商品")
    public ResponseEntity<Void> updateSaleable(@RequestParam("id") Long id, @RequestParam("saleable") Boolean saleable) {
        goodsService.updateSaleable(id, saleable);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/spu/detail", name = "查询商品详细信息")
    public ResponseEntity<SpuDetailDTO> findSpuDetailBySpuId(@RequestParam("id") Long id) {
        SpuDetailDTO spuDetailDTO=goodsService.findSpuDetailBySpuId(id);
        return ResponseEntity.ok(spuDetailDTO);
    }
    @GetMapping(value = "/sku/of/spu",name = "根据spuId查询sku数据")
    public ResponseEntity<List<SkuDTO>> findSkuListBySpuId(@RequestParam("id")Long id){
       List<SkuDTO> skuDTOList= goodsService.findSkuListBySpuId(id);
       return ResponseEntity.ok(skuDTOList);
    }
    @PutMapping(value = "/goods", name = "修改商品信息")
    public ResponseEntity<Void> updateGoods(@RequestBody SpuDTO spuDTO) {
        goodsService.updateGoods(spuDTO);
        return ResponseEntity.ok().build();
    }
}
