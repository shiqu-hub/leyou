package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.TbBrand;
import com.leyou.item.entity.TbCategoryBrand;
import com.leyou.item.mapper.TbBrandMapper;
import com.leyou.item.service.TbBrandService;
import com.leyou.item.service.TbCategoryBrandService;
import com.leyou.item.service.TbCategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 服务实现类
 * </p>
 *
 * @author kai
 * @since 2019-12-24
 */
@Service
public class TbBrandServiceImpl extends ServiceImpl<TbBrandMapper, TbBrand> implements TbBrandService {

    @Autowired
    private TbCategoryBrandService categoryBrandService;

    @Override
    public PageResult<BrandDTO> findPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {
        Page<TbBrand> p = new Page<TbBrand>(page, rows);
        QueryWrapper<TbBrand> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(key)) {
            //name like "%AA%" or letter="A"
            queryWrapper.lambda().like(TbBrand::getName, key).or().eq(TbBrand::getLetter, key);
        }
        if (StringUtils.isNotBlank(sortBy)) {
            if (desc) {
                p.setDesc(sortBy);
            } else {
                p.setAsc(sortBy);
            }
        }
        IPage<TbBrand> iPage = this.page(p, queryWrapper);
        //当前页数据
        List<TbBrand> brandList = iPage.getRecords();

        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //总条数
        long total = iPage.getTotal();

        List<BrandDTO> brandDTOList = BeanHelper.copyWithCollection(brandList, BrandDTO.class);
        return new PageResult<BrandDTO>(total, brandDTOList);
    }

    @Override
    @Transactional
    public void saveBrandAndCategory(BrandDTO brandDTO, List<Long> cids) {
//      1.保存品牌数据
        TbBrand tbBrand = BeanHelper.copyProperties(brandDTO, TbBrand.class);
        tbBrand.setUpdateTime(new Date());
        tbBrand.setCreateTime(new Date());
//       this.isInsert
        boolean save = this.save(tbBrand);
        if (!save) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//       2.保存品牌分类中间表数据
        Long brandId = tbBrand.getId();
        for (Long cid : cids) {
            TbCategoryBrand categoryBrand = new TbCategoryBrand();
            categoryBrand.setCategoryId(cid);
            categoryBrand.setBrandId(brandId);
            //categoryBrand.insert();
            categoryBrandService.save(categoryBrand);
        }
    }

    @Override
    public void updateBrandAndCategory(BrandDTO brandDTO, List<Long> cids) {
//     1.保存品牌数据
        TbBrand tbBrand = BeanHelper.copyProperties(brandDTO, TbBrand.class);
        tbBrand.setUpdateTime(new Date());
        boolean isUpdate = this.updateById(tbBrand);
        if (!isUpdate) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//      2.删除中间表数据
        Long brandId = tbBrand.getId();
        if (!CollectionUtils.isEmpty(cids)) {
            QueryWrapper<TbCategoryBrand> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(TbCategoryBrand::getBrandId, brandId);
            boolean isRemove = categoryBrandService.remove(queryWrapper);
            if (!isRemove) {
                throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
            }
//      3.新增中间表数据
            List<TbCategoryBrand> list = new ArrayList<>(cids.size());
            for (Long cid : cids) {
                TbCategoryBrand categoryBrand = new TbCategoryBrand();
                categoryBrand.setBrandId(brandId);
                categoryBrand.setCategoryId(cid);
                list.add(categoryBrand);
            }
            categoryBrandService.saveBatch(list);
        }
    }

    @Override
    public List<BrandDTO> findBrandByCategoryId(Long id) {
        List<TbBrand> brandList = this.getBaseMapper().findBrandByCategoryId(id);
        if (CollectionUtils.isEmpty(brandList)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(brandList, BrandDTO.class);
    }

    @Override
    public List<BrandDTO> findBrandsByIds(List<Long> ids) {
        Collection<TbBrand> tbBrandCollection = this.listByIds(ids);
        if (CollectionUtils.isEmpty(tbBrandCollection)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        List<BrandDTO> brandDTOList=tbBrandCollection.stream().map(tbBrand -> {
            return BeanHelper.copyProperties(tbBrand,BrandDTO.class);
        }).collect(Collectors.toList());
        return brandDTOList;
    }
}
