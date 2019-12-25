package com.leyou.item.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyou.item.entity.TbBrand;


/**
 * <p>
 * 品牌表，一个品牌下有多个商品（spu），一对多关系 Mapper 接口
 * </p>
 *
 * @author kai
 * @since 2019-12-24
 */
public interface TbBrandMapper extends BaseMapper<TbBrand> {

}
