package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.SkuDTO;
import com.leyou.item.dto.SpuDTO;
import com.leyou.item.dto.SpuDetailDTO;
import com.leyou.item.entity.*;
import com.leyou.item.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private TbSpuService SpuService;
    @Autowired
    private TbBrandService brandService;
    @Autowired
    private TbCategoryService categoryService;
    @Autowired
    private TbSpuDetailService spuDetailService;
    @Autowired
    private TbSkuService skuService;


    @Override
    public PageResult<SpuDTO> findSpuByPage(Integer page, Integer rows, String key, Boolean saleable) {
        //分页
        Page<TbSpu> p = new Page<TbSpu>(page, rows);
        //构建查询条件
        QueryWrapper<TbSpu> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(key)) {
            queryWrapper.lambda().like(TbSpu::getName, key);
        }
        if (saleable != null) {
            queryWrapper.lambda().eq(TbSpu::getSaleable, saleable);
        }
        IPage<TbSpu> iPage = SpuService.page(p, queryWrapper);
        List<TbSpu> tbSpuList = iPage.getRecords();
        if (CollectionUtils.isEmpty(tbSpuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SpuDTO> spuDTOList = BeanHelper.copyWithCollection(tbSpuList, SpuDTO.class);
        spuDTOList = handlerBrandAndCategoryName(spuDTOList);
        return new PageResult<SpuDTO>(iPage.getTotal(), spuDTOList);
    }

    @Override
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
        //统一将对象转为对应数据库的对象，spu与spuDetail为1对1的关系，spu与sku为1对多的关系
        //保存spu数据
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        boolean result = SpuService.save(tbSpu);
        if (!result) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //保存spuDetail数据
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDetailDTO, TbSpuDetail.class);
        //将表spuDetail中的id与spu表中的id绑定
        tbSpuDetail.setSpuId(tbSpu.getId());
        boolean result1 = spuDetailService.save(tbSpuDetail);
        if (!result1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //保存sku数据
        List<SkuDTO> skuDTOList = spuDTO.getSkus();
        List<TbSku> tbSkuList = BeanHelper.copyWithCollection(skuDTOList, TbSku.class);
        //将集合中的每个sku的id与spu的id绑定
        tbSkuList = tbSkuList.stream().map(sku -> {
            sku.setSpuId(tbSpu.getId());
            return sku;
        }).collect(Collectors.toList());
        boolean result2 = skuService.saveBatch(tbSkuList);
        if (!result2) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Override
    public void updateSaleable(Long id, Boolean saleable) {
//     1.更新spu
        TbSpu tbSpu = new TbSpu();
        tbSpu.setId(id);
        tbSpu.setSaleable(saleable);
        boolean result = SpuService.updateById(tbSpu);
        if (!result) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
//     2.更新sku
        UpdateWrapper<TbSku> updateWrapper = new UpdateWrapper<>();
        //设置需要更新的内容，也可以使用lambda的方式
        updateWrapper.set("enable", saleable);
        updateWrapper.eq("spu_id", id);
        boolean result1 = skuService.update(updateWrapper);
        if (!result1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
    }

    @Override
    public SpuDetailDTO findSpuDetailBySpuId(Long spuId) {
        TbSpuDetail spuDetail = spuDetailService.getById(spuId);
        if (spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }

    @Override
    public List<SkuDTO> findSkuListBySpuId(Long spuId) {
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuId);
        List<TbSku> skuList = skuService.list(queryWrapper);
        if (CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        List<SkuDTO> skuDTOList = BeanHelper.copyWithCollection(skuList, SkuDTO.class);
        return skuDTOList;

    }

    @Override
    @Transactional
    public void updateGoods(SpuDTO spuDTO) {
        //统一将对象转为对应数据库的对象，spu与spuDetail为1对1的关系，spu与sku为1对多的关系
//       1.保存spu数据
        TbSpu tbSpu = BeanHelper.copyProperties(spuDTO, TbSpu.class);
        tbSpu.setUpdateTime(new Date());
        boolean result = SpuService.updateById(tbSpu);
        if (!result) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//       2.保存spuDetail数据
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        TbSpuDetail tbSpuDetail = BeanHelper.copyProperties(spuDetailDTO, TbSpuDetail.class);
        //将表spuDetail中的id与spu表中的id绑定
        tbSpuDetail.setSpuId(tbSpu.getId());
        tbSpuDetail.setUpdateTime(new Date());
        boolean result1 = spuDetailService.updateById(tbSpuDetail);
        if (!result1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
//       3.先删除sku数据
        QueryWrapper<TbSku> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSku::getSpuId,spuDTO.getId());
        skuService.remove(queryWrapper);
//       4.保存sku数据
        List<SkuDTO> skuDTOList = spuDTO.getSkus();
        List<TbSku> tbSkuList = BeanHelper.copyWithCollection(skuDTOList, TbSku.class);
        //将集合中的每个sku的id与spu的id绑定
        tbSkuList = tbSkuList.stream().map(sku -> {
            sku.setSpuId(tbSpu.getId());
            sku.setUpdateTime(new Date());
            return sku;
        }).collect(Collectors.toList());
        boolean result2 = skuService.saveBatch(tbSkuList);
        if (!result2) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    private List<SpuDTO> handlerBrandAndCategoryName(List<SpuDTO> spuDTOList) {
        for (SpuDTO spuDTO : spuDTOList) {
            //处理品牌名称
            TbBrand brand = brandService.getById(spuDTO.getBrandId());
            if (brand != null) {
                spuDTO.setBrandName(brand.getName());
            }
            //处理分类名称 cid1  cid2  cid3 --->名称1/名称2/名称3
            Collection<TbCategory> categoryList = categoryService.listByIds(spuDTO.getCategoryIds());
            String categoryNames = categoryList.stream().map(TbCategory::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryNames);
        }
        return spuDTOList;
    }
}
