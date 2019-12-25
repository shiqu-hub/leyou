package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.mapper.TbSpecGroupMapper;
import com.leyou.item.service.TbSpecGroupService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 规格参数的分组表，每个商品分类下有多个规格参数组 服务实现类
 * </p>
 *
 * @author kai
 * @since 2019-12-25
 */
@Service
public class TbSpecGroupServiceImpl extends ServiceImpl<TbSpecGroupMapper, TbSpecGroup> implements TbSpecGroupService {

    @Override
    public List<SpecGroupDTO> findSpecGroupByCategoryId(Long categoryId) {
        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid,categoryId);

        List<TbSpecGroup> specGroupList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(specGroupList)){
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        List<SpecGroupDTO> groupDTOList = BeanHelper.copyWithCollection(specGroupList, SpecGroupDTO.class);
        return groupDTOList;
    }
}
