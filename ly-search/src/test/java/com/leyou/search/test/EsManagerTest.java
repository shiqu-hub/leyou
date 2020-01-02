package com.leyou.search.test;


import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.SpuDTO;
import com.leyou.search.entity.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EsManagerTest {
    @Autowired
    private ItemClient itemClient;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SearchService goodsService;
    @Test
    public void test() {
        // 从数据库中查询spu数据
        //一个goods对应一个spu
        int page = 1;
        int rows = 10;
        while (true) {
            PageResult<SpuDTO> spuByPageList = itemClient.findSpuByPage(page, rows, null, true);
            if (spuByPageList == null || CollectionUtils.isEmpty(spuByPageList.getItems())){
                break;
            }
            //把spu------>Goods
            List<SpuDTO> spuDTOList = spuByPageList.getItems();
            //SpuDTO[] spuDTOS = spuDTOList.toArray(new SpuDTO[0]);
            ArrayList<Goods> goodsList = new ArrayList<>(spuDTOList.size());
            for (SpuDTO spuDTO : spuDTOList) {
                Goods goods= goodsService.buildGoods(spuDTO);
                goodsList.add(goods);
            }
            //保存到es中
            goodsRepository.saveAll(goodsList);
            //退出条件
            if(spuDTOList.size()<rows){
                break;
            }
            page++;
        }
        elasticsearchTemplate.putMapping(Goods.class);
    }
    @Test
    public void testSearch(){
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构建查询条件
        queryBuilder.withQuery(QueryBuilders.matchQuery("all","note"));
        //分页
        Pageable pageable = PageRequest.of(0,2);
        queryBuilder.withPageable(pageable);
        //过滤查询结果的显示
        queryBuilder.withSourceFilter(new FetchSourceFilter(null,new String[]{"specs"}));
        AggregatedPage<Goods> aggregatedPage = elasticsearchTemplate.queryForPage(queryBuilder.build(), Goods.class);
        List<Goods> goodsList = aggregatedPage.getContent();
        System.out.println("查询出的总条数"+aggregatedPage.getTotalElements());
        System.out.println(goodsList);
    }
}
