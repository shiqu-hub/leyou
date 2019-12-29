package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface ItemClient {

    @GetMapping(value = "/brand/of/category", name = "根据分类id查询品牌信息")
    public List<BrandDTO> findBrandByCategory(@RequestParam("id") Long id);

    @GetMapping(value = "/spu/page", name = "分页查询商品SPU信息")
    public PageResult<SpuDTO> findSpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable);

    @GetMapping(value = "/sku/of/spu",name = "根据spuId查询sku数据")
    public List<SkuDTO> findSkuListBySpuId(@RequestParam("id")Long id);

    @GetMapping(value = "/spec/params", name = "根据分类ID查询规格组信息")
    public List<SpecParamDTO> findSpecParamByCategoryId(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);

    @GetMapping(value = "/spu/detail", name = "查询商品详细信息")
    public SpuDetailDTO findSpuDetailBySpuId(@RequestParam("id") Long id);

}
