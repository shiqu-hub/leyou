package com.leyou.page.service;


import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {
    @Autowired
    private ItemClient itemClient;

    public Map buildDataBySpuId(Long id) {
        Map<String, Object> dataMap = new HashMap<>();
        //根据spuId查询spu对象
        SpuDTO spu = itemClient.findSpuById(id);
        //spuName 来自spu对象
        dataMap.put("spuName", spu.getName());
        //subTitle 来自spu对象
        dataMap.put("subTitle", spu.getSubTitle());
        //categories 三级分类的对象集合
        List<Long> categoryIds = spu.getCategoryIds();
        List<CategoryDTO> categoryDTOList = itemClient.findCategorysByIds(categoryIds);
        dataMap.put("categories", categoryDTOList);
        //brand对象
        Long brandId = spu.getBrandId();
        BrandDTO brand = itemClient.findBrandById(brandId);
        dataMap.put("brand", brand);
        //detail对象
        SpuDetailDTO spuDetailDTO = itemClient.findSpuDetailBySpuId(spu.getId());
        dataMap.put("detail", spuDetailDTO);
        //skus sku集合
        List<SkuDTO> skuList = itemClient.findSkuListBySpuId(spu.getId());
        dataMap.put("skus", skuList);
        //specs规格组的集合
        List<SpecGroupDTO> paramGroupList = itemClient.findSpecGroupWithParamListByCategoryId(spu.getCid3());
        dataMap.put("specs", paramGroupList);

        return dataMap;
    }
}
