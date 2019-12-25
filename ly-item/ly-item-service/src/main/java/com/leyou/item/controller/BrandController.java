package com.leyou.item.controller;


import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.service.TbBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController{

    @Autowired
    private TbBrandService brandService;

    @GetMapping(value = "/page",name = "分页查询品牌数据")
    public ResponseEntity<PageResult<BrandDTO>> findPage(
            @RequestParam("key") String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc",required = true,defaultValue = "false") Boolean desc){
        PageResult<BrandDTO> brandPage = brandService.findPage(key,page,rows,sortBy,desc);
        return ResponseEntity.ok(brandPage);
    }
    @PostMapping(name = "保存品牌数据")
    public ResponseEntity<Void> save(BrandDTO brandDTO,@RequestParam("cids") List<Long> cids){
        brandService.saveBrandAndCategory(brandDTO,cids);
        return ResponseEntity.ok().build();
    }
    @PutMapping(name = "修改品牌数据")
    public ResponseEntity<Void> update(BrandDTO brandDTO,@RequestParam(name = "cids") List<Long> cids) {
        brandService.updateBrandAndCategory(brandDTO,cids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
