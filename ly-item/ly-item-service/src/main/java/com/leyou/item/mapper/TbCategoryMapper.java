package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.TbCategory;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 * 商品类目表，类目和商品(spu)是一对多关系，类目与品牌是多对多关系 Mapper 接口
 * </p>
 *
 * @author kai
 * @since 2019-12-24
 */
public interface TbCategoryMapper extends BaseMapper<TbCategory> {
@Select("select c.* from tb_category c,tb_category_brand cb where c.id=cb.category_id and cb.brand_id=#{brandId}")
    List<TbCategory> selectCategoryByBrandId(@Param("brandId") Long brandId);
}
