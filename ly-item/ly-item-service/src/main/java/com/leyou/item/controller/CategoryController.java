package com.leyou.item.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.service.TbCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private TbCategoryService categoryService;

    @RequestMapping(value = "/of/parent",name = "根据parentid获取分类信息")
    //@CrossOrigin(origins = "http://manage.leyou.com")
    public ResponseEntity<List<CategoryDTO>> findByParentId(@RequestParam("pid") Long pid) {
        //创建一个用来构建条件的对象
        QueryWrapper<TbCategory> queryWrapper = new QueryWrapper<TbCategory>();
        //使用lamda方式添加条件
        queryWrapper.lambda().eq(TbCategory::getParentId, pid);
        //执行查询
        List<TbCategory> categoryList = categoryService.list(queryWrapper);

        List<CategoryDTO> categoryDTOList = BeanHelper.copyWithCollection(categoryList, CategoryDTO.class);
        //return ResponseEntity.status(HttpStatus.OK).body(categoryDTOList);
        return ResponseEntity.ok(categoryDTOList);
    }

    @GetMapping(value = "/of/brand",name = "根据品牌id查询分类数据")
    public ResponseEntity<List<CategoryDTO>> findCategoryListByBrandId(@RequestParam(name = "id") Long brandId) {
        return ResponseEntity.ok(categoryService.findCategoryListByBrandId(brandId));
    }
    @GetMapping(value = "/list",name = "根据分类id查询分类数据")
public ResponseEntity<List<CategoryDTO>> findCategorysByIds(@RequestParam("ids") List<Long> ids){
       return ResponseEntity.ok(categoryService.findCategorysByIds(ids));
    }
}
