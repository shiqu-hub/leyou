package com.leyou.item.controller;


import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.service.TbBrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/brand")
public class BrandController {

    @Autowired
    private TbBrandService brandService;

    /**
     * @param key    查询条件
     * @param page   当前页
     * @param rows   每页显示个数
     * @param sortBy 排序字段
     * @param desc   是否降序
     * @return
     */
    @GetMapping(value = "/page", name = "分页查询品牌数据")
    public ResponseEntity<PageResult<BrandDTO>> findPage(
            @RequestParam("key") String key,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", required = true, defaultValue = "false") Boolean desc) {
        PageResult<BrandDTO> brandPage = brandService.findPage(key, page, rows, sortBy, desc);
        return ResponseEntity.ok(brandPage);
    }

    @PostMapping(name = "保存品牌数据")
    public ResponseEntity<Void> save(BrandDTO brandDTO, @RequestParam("cids") List<Long> cids) {
        brandService.saveBrandAndCategory(brandDTO, cids);
        return ResponseEntity.ok().build();
    }

    @PutMapping(name = "修改品牌数据")
    public ResponseEntity<Void> update(BrandDTO brandDTO, @RequestParam(name = "cids") List<Long> cids) {
        brandService.updateBrandAndCategory(brandDTO, cids);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(value = "/of/category", name = "根据分类id查询品牌信息")
    public ResponseEntity<List<BrandDTO>> findBrandByCategory(@RequestParam("id") Long id) {
        List<BrandDTO> brandDTOList = brandService.findBrandByCategoryId(id);
        return ResponseEntity.ok(brandDTOList);
    }

    @GetMapping(value = "/list",name = "查询品牌集合")
    public ResponseEntity<List<BrandDTO>> findBrandsByIds(@RequestParam(name = "ids") List<Long> ids){
        List<BrandDTO> brandDTOList = brandService.findBrandsByIds(ids);
        return ResponseEntity.ok(brandDTOList);
    }

    @GetMapping(value = "/{id}",name = "根据品牌id查询品牌")
    public ResponseEntity<BrandDTO> findBrandById(@PathVariable(value = "id") Long id){
        TbBrand tbBrand = brandService.getById(id);
        return ResponseEntity.ok(BeanHelper.copyProperties(tbBrand,BrandDTO.class));
    }
}
