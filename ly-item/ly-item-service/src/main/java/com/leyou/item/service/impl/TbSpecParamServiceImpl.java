package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.mapper.TbSpecParamMapper;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 规格参数组下的参数名 服务实现类
 * </p>
 *
 * @author kai
 * @since 2019-12-25
 */
@Service
public class TbSpecParamServiceImpl extends ServiceImpl<TbSpecParamMapper, TbSpecParam> implements TbSpecParamService {

    @Override
    public List<SpecParamDTO> findSpecParamByCategoryId(Long gid, Long cid, Boolean searching) {
//      分组cid 和 参数gid必须存在一个
        if (gid == null && cid == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        if (cid != null) {
            queryWrapper.lambda().eq(TbSpecParam::getCid, cid);
        }
        if (gid != null) {
            queryWrapper.lambda().eq(TbSpecParam::getGroupId, gid);
        }
        if (searching != null) {
            queryWrapper.lambda().eq(TbSpecParam::getSearching, searching);
        }
        List<TbSpecParam> specParamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(specParamList)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(specParamList,SpecParamDTO.class);
    }
}
