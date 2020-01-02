package com.leyou.search.controller;

import com.leyou.common.vo.PageResult;
import com.leyou.search.dto.GoodsDTO;
import com.leyou.search.dto.SearchRequest;
import com.leyou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    @PostMapping("/page")
    public ResponseEntity<PageResult<GoodsDTO>> search(@RequestBody SearchRequest searchRequest) {
        return ResponseEntity.ok(searchService.findGoodsByPage(searchRequest));
    }
    @PostMapping(value = "/filter",name = "查询过滤项")
    public ResponseEntity<Map<String, List<?>>> queryFilters(@RequestBody SearchRequest searchRequest){
        return ResponseEntity.ok(searchService.queryFilters(searchRequest));
    }
}
