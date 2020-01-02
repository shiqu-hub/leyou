package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.entity.Goods;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private ItemClient itemClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

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
        Map<Long, List<String>> specialSpecMap = JsonUtils.nativeRead(specialSpec, new TypeReference<Map<Long, List<String>>>() {
        });

        //{"1":"华为（HUAWEI）","2":"华为麦芒5（HUAWEI MLA-AL10）","3":2016.0,"5":160,"6":"其它","7":"Android","8":"骁龙（Snapdragon)","9":"骁龙625（MSM8953）","10":"八核","11":2.0,"14":5.5,"15":"1920*1080(FHD)","16":800.0,"17":1600.0,"18":3340.0}
        //  特有规格参数值
        String genericSpec = spuDetail.getGenericSpec();
        Map<Long, String> genericSpecMap = JsonUtils.toMap(genericSpec, Long.class, String.class);

        // 获取规格参数key，来自于SpecParam中当前分类下的需要搜索的规格
        List<SpecParamDTO> specParamList = itemClient.findSpecParamByCategoryId(null, spuDTO.getCid3(), true);
        Map<String, Object> specsMap = new HashMap<>();
        for (SpecParamDTO param : specParamList) {
            // 获取规格参数的名称
            String key = param.getName();
            // 获取规格参数值
            Object value = null;
            // 判断是否是通用规格
            if (param.getGeneric()) {
                // 通用规格
                value = genericSpecMap.get(param.getId());
            } else {
                // 特有规格
                value = specialSpecMap.get(param.getId());
            }
            // 判断是否是数字类型
            if (param.getIsNumeric()) {
                // 是数字类型，分段
                value = chooseSegment(value, param);
            }
            // 添加到specs
            specsMap.put(key, value);
        }
        //{"品牌":"小米","操作系统":"Android","屏幕尺寸":'5.5-6.0'}
        // 当前spu的规格参数
        goods.setSpecs(specsMap);

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

    public PageResult<GoodsDTO> findGoodsByPage(SearchRequest searchRequest) {
        //判断是否输入查询条件
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)) {
            return null;
        }
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        //构建查询条件
        searchQueryBuilder = buildBaseQuery(searchRequest, searchQueryBuilder);
        Integer page = searchRequest.getPage();
        Integer size = searchRequest.getSize();
        //分页
        searchQueryBuilder.withPageable(PageRequest.of(page - 1, size));
        //筛选查询结果
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));


        AggregatedPage<Goods> aggregatedPage = elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), Goods.class);
        Integer totalPages = aggregatedPage.getTotalPages();
        long totalElements = aggregatedPage.getTotalElements();
        List<Goods> goodsList = aggregatedPage.getContent();

        List<GoodsDTO> goodsDTOList = BeanHelper.copyWithCollection(goodsList, GoodsDTO.class);
        return new PageResult<GoodsDTO>(totalElements, totalPages.longValue(), goodsDTOList);
    }
    //    构建基本查询
    private NativeSearchQueryBuilder buildBaseQuery(SearchRequest searchRequest, NativeSearchQueryBuilder searchQueryBuilder) {
        //根据关键字查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery().must(QueryBuilders.matchQuery("all", searchRequest.getKey()));
        //追加过滤条件
        Map<String, String> filterMap = searchRequest.getFilterMap();
        for (String key : filterMap.keySet()) {
            if ("品牌".equals(key)) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("brandId", filterMap.get(key)));
            } else if ("分类".equals(key)) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("categoryId", filterMap.get(key)));
            } else {
                //跟规格有关系的过滤条件
                boolQueryBuilder.filter(QueryBuilders.termQuery("specs." + key, filterMap.get(key)));
            }
        }
        searchQueryBuilder.withQuery(boolQueryBuilder);
        return searchQueryBuilder;
    }

    public Map<String, List<?>> queryFilters(SearchRequest searchRequest) {
        //创建用来存储的map集合
        Map<String, List<?>> filterMap = new HashMap<>();
        //判断是否输入查询条件
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)) {
            return null;
        }
        NativeSearchQueryBuilder searchQueryBuilder = new NativeSearchQueryBuilder();
        //构建查询条件
        searchQueryBuilder = buildBaseQuery(searchRequest, searchQueryBuilder);
        //分页
        searchQueryBuilder.withPageable(PageRequest.of(0, 1));
        //筛选查询结果
        searchQueryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id"}, null));
        //构建聚合品牌的条件
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("brandAgg").field("brandId").size(100));
        //构建聚合分类的条件
        searchQueryBuilder.addAggregation(AggregationBuilders.terms("categoryAgg").field("categoryId").size(100));

        AggregatedPage<Goods> aggregatedPage = elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), Goods.class);
        Aggregations aggregations = aggregatedPage.getAggregations();

        handlerBrandAgg(filterMap, aggregations);
        handlerCategoryAgg(filterMap, searchQueryBuilder, aggregations);

        return filterMap;
    }
    //处理分类聚合结果
    private void handlerCategoryAgg(Map<String, List<?>> filterMap, NativeSearchQueryBuilder searchQueryBuilder, Aggregations aggregations) {
        //获取分类聚合结果
        Terms categoryTerms = aggregations.get("categoryAgg");
        List<? extends Terms.Bucket> categoryBuckets = categoryTerms.getBuckets();
        List<Long> categoryIdList = categoryBuckets.stream().map(Terms.Bucket::getKeyAsNumber).map(Number::longValue).collect(Collectors.toList());
        //根据品牌id 获取品牌
        List<CategoryDTO> categoryDTOList = itemClient.findCategorysByIds(categoryIdList);
        filterMap.put("分类", categoryDTOList);
        if (!CollectionUtils.isEmpty(categoryIdList)) {
            //根据第一个分类展示规格数据
            Long categoryId = categoryIdList.get(0);
            //查询分类下的规格参数
            List<SpecParamDTO> specParamList = itemClient.findSpecParamByCategoryId(null, categoryId, true);
            for (SpecParamDTO specParamDTO : specParamList) {
                String name = specParamDTO.getName();
                searchQueryBuilder.addAggregation(AggregationBuilders.terms(name + "Agg").field("specs." + name).size(10));
            }
            //有了分类，才能知道聚合那些规格参数，执行聚合查询
            AggregatedPage<Goods> aggregatedPage = elasticsearchTemplate.queryForPage(searchQueryBuilder.build(), Goods.class);
            Aggregations specAggregations = aggregatedPage.getAggregations();
            for (SpecParamDTO specParamDTO : specParamList) {
                String name = specParamDTO.getName();
                Terms specTerms = specAggregations.get(name + "Agg");
                List<? extends Terms.Bucket> specBuckets = specTerms.getBuckets();
                List<String> specList = specBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                filterMap.put(name, specList);
            }
        }
    }
    //处理品牌的聚合结果
    private void handlerBrandAgg(Map<String, List<?>> filterMap, Aggregations aggregations) {
        //获取品牌聚合结果
        Terms brandAgg = aggregations.get("brandAgg");
        List<? extends Terms.Bucket> buckets = brandAgg.getBuckets();
        List<Long> brandIdList = buckets.stream().map(Terms.Bucket::getKeyAsNumber).map(Number::longValue).collect(Collectors.toList());
        //根据品牌id 获取品牌
        List<BrandDTO> brandDTOList = itemClient.findBrandsByIds(brandIdList);
        filterMap.put("品牌", brandDTOList);
    }
}
