package com.leyou.item.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("item-service")
public interface ItemClient {
    /**
     *
     * @param id  分类的id
     * @return   品牌的集合
     */
    @GetMapping(value = "/brand/of/category", name = "根据分类id查询品牌信息")
    public List<BrandDTO> findBrandByCategory(@RequestParam("id") Long id);

    /**
     *
     * @param page  当前页
     * @param rows  每页个数
     * @param key    关键字
     * @param saleable   是否上下架
     * @return    spu的集合
     */
    @GetMapping(value = "/spu/page", name = "分页查询商品SPU信息")
    public PageResult<SpuDTO> findSpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable", required = false) Boolean saleable);

    /**
     *
     * @param id  spuId
     * @return   sku的集合
     */
    @GetMapping(value = "/sku/of/spu",name = "根据spuId查询sku数据")
    public List<SkuDTO> findSkuListBySpuId(@RequestParam("id")Long id);

    /**
     *
     * @param gid   分组的id
     * @param cid   商品的id
     * @param searching  搜索的关键字
     * @return   规格信息的集合
     */
    @GetMapping(value = "/spec/params", name = "根据分类ID查询规格组信息")
    public List<SpecParamDTO> findSpecParamByCategoryId(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching);

    /**
     *
     * @param id  spuId
     * @return   商品的详细信息
     */
    @GetMapping(value = "/spu/detail", name = "查询商品详细信息")
    public SpuDetailDTO findSpuDetailBySpuId(@RequestParam("id") Long id);

    /**
     *
     * @param ids 品牌id的集合
     * @return  品牌的集合
     */
    @GetMapping(value = "/brand/list",name = "根据品牌id批量查询品牌")
    public List<BrandDTO> findBrandsByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据分类id查询分类数据
     * @param ids  商品id的集合
     * @return     商品的集合
     */
    @GetMapping(value = "/category/list",name = "根据分类id查询分类数据")
    public List<CategoryDTO> findCategorysByIds(@RequestParam("ids") List<Long> ids);

    /**
     * 根据spu的id查询spu
     * @param id spu的id
     * @return  spu对象
     */
    @GetMapping(value = "/spu/{id}",name = "根据spuId查询spu对象")
    public SpuDTO findSpuById(@PathVariable("id") Long id);

    /**
     * 根据品牌id查询品牌
     * @param id brand的id
     * @return brandDTO对象
     */
    @GetMapping(value = "/brand/{id}",name = "根据品牌id查询品牌")
    public BrandDTO findBrandById(@PathVariable(name = "id") Long id);

    /**
     * 根据categoryId查询规格参数组合组内参数
     * @param id 商品的id
     * @return  规格组的集合
     */
    @GetMapping(value = "/spec/of/category", name = "根据categoryId查询规格参数组合组内参数")
    public List<SpecGroupDTO> findSpecGroupWithParamListByCategoryId(@RequestParam("id") Long id);

    /**
     * 根据id批量查询sku
     * @param ids skuId的集合
     * @return sku的集合
     */
    @GetMapping(value = "/sku/list",name = "根据skuId集合查询sku数据")
    public List<SkuDTO> querySkuByIds(@RequestParam("ids") List<Long> ids);

}



