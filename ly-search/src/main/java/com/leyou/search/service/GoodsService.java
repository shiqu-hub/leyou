package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.search.entity.Goods;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private ItemClient itemClient;

    public Goods buildGoods(SpuDTO spuDTO) {
        Goods goods = new Goods();

        Long spuId = spuDTO.getId();
        goods.setId(spuId);
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setBrandId(spuDTO.getBrandId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());

        //商品名称+品牌名称+分类名称
        goods.setAll(spuDTO.getName() + spuDTO.getBrandName() + spuDTO.getCategoryName());
        //  spu下的所有sku的JSON数组
        List<SkuDTO> skuDTOList = itemClient.findSkuListBySpuId(spuId);
        // 准备一个集合，用map来代替sku，只需要sku中的部分数据
        List<Map> skus = skuDTOList.stream().map(skuDTO -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skuDTO.getId());
            map.put("price", skuDTO.getPrice());
            map.put("title", skuDTO.getTitle());
            //http://image.leyou.com/images/14/7/1524297352221.jpg,http://image.leyou.com/images/14/7/1524297352221.jpg
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(), ","));
            return map;
        }).collect(Collectors.toList());
        //"[{id:11,price:1999,image:"",title:""}{id:11,price:2999,image:"",title:""}]"
        goods.setSkus(JsonUtils.toString(skus));

        //价格等于skus中price的值
        // 当前spu下所有sku的价格的集合
        Set<Long> prices = skuDTOList.stream().map(SkuDTO::getPrice).collect(Collectors.toSet());
        goods.setPrice(prices);
        //  获取规格参数的值，来自于spuDetail
        SpuDetailDTO spuDetail = itemClient.findSpuDetailBySpuId(spuId);

        //{"4":["香槟金","苍穹灰"],"12":["4GB"],"13":["64GB"]}
        //  通用规格参数值
        String specialSpec = spuDetail.getSpecialSpec();
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {});

        //{"1":"华为（HUAWEI）","2":"华为麦芒5（HUAWEI MLA-AL10）","3":2016.0,"5":160,"6":"其它","7":"Android","8":"骁龙（Snapdragon)","9":"骁龙625（MSM8953）","10":"八核","11":2.0,"14":5.5,"15":"1920*1080(FHD)","16":800.0,"17":1600.0,"18":3340.0}
        //  特有规格参数值
        String genericSpec = spuDetail.getGenericSpec();
        Map<Long, String> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class, String.class);

        // 获取规格参数key，来自于SpecParam中当前分类下的需要搜索的规格
        List<SpecParamDTO> specParamList = itemClient.findSpecParamByCategoryId(null, spuDTO.getCid3(), true);
        Map<String,Object> specsMap =new HashMap<>();
        for (SpecParamDTO param : specParamList) {
            // 获取规格参数的名称
            String key = param.getName();
            // 获取规格参数值
            Object value=null;
            // 判断是否是通用规格
            if (param.getGeneric()){
                // 通用规格
                value = genericSpecMap.get(param.getId());
            }else {
                // 特有规格
                value = specialSpecMap.get(param.getId());
            }
            // 判断是否是数字类型
            if (param.getIsNumeric()){
                // 是数字类型，分段
               value = chooseSegment(value,param);
            }
            // 添加到specs
            specsMap.put(key,value);
        }
        //{"品牌":"小米","操作系统":"Android","屏幕尺寸":'5.5-6.0'}
        goods.setSpecs(specsMap); // 当前spu的规格参数

        return goods;
    }


    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = parseDouble(value.toString());
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = parseDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = parseDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    private double parseDouble(String str) {
        try {
            return Double.parseDouble(str);
        } catch (Exception e) {
            return 0;
        }
    }

}
