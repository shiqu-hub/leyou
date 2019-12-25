package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.TbCategory;
import com.leyou.item.mapper.TbCategoryMapper;
import com.leyou.item.service.TbCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 服务实现类
 * </p>
 *
 * @author kai
 * @since 2019-12-24
 */
@Service
public class TbCategoryServiceImpl extends ServiceImpl<TbCategoryMapper, TbCategory> implements TbCategoryService {

    @Override
    public List<CategoryDTO> findCategoryListByBrandId(Long brandId) {
        List<TbCategory> tbCategoryList = this.getBaseMapper().selectCategoryByBrandId(brandId);
        if (CollectionUtils.isEmpty(tbCategoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbCategoryList,CategoryDTO.class);
    }
}
