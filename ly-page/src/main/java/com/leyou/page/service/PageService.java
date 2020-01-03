package com.leyou.page.service;


import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class PageService {
    @Autowired
    private ItemClient itemClient;
    @Autowired
    private TemplateEngine templateEngine;

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

    public void createHtml(Long spuId) {
        Context context = new Context();
        context.setVariables(this.buildDataBySpuId(spuId));
        try(PrintWriter printWriter = new PrintWriter("D:\\develop\\nginx-1.14.0\\html\\item\\" + spuId + ".html")) {
            templateEngine.process("item",context,printWriter);
            log.debug("静态页面成功创建完成");
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
        }

    }

    public void removeHtml(Long spuId) {
        File file = new File("D:\\develop\\nginx-1.14.0\\html\\item\\" + spuId + ".html");
        if (file.exists()){
            if (!file.delete()){
                log.error("【静态页服务】静态页删除失败，商品id：{}", spuId);
                throw new LyException(ExceptionEnum.FILE_WRITER_ERROR);
            }
        }
        log.info("成功删除静态页面");
    }
}
