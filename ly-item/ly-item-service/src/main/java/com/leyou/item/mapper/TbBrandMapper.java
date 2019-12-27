package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.TbBrand;
import org.apache.ibatis.annotations.Select;

import java.util.List;


/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 Mapper 接口
 * </p>
 *
 * @author kai
 * @since 2019-12-24
 */
public interface TbBrandMapper extends BaseMapper<TbBrand> {
    @Select("select * from tb_category_brand cb,tb_brand b where cb.brand_id=b.id and cb.category_id=#{id}")
    List<TbBrand> findBrandByCategoryId(Long id);
}
