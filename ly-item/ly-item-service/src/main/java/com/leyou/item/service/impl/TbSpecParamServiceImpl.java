package com.leyou.item.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.mapper.TbSpecParamMapper;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private TbSpecGroupService specGroupService;

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
        return BeanHelper.copyWithCollection(specParamList, SpecParamDTO.class);
    }

    @Override
    public void saveSpecParam(TbSpecParam specParam) {
        if (specParam.getName() == null) {
            throw new LyException(ExceptionEnum.INVALID_PARAM_ERROR);
        }
        QueryWrapper<TbSpecParam> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecParam::getCid, specParam.getCid()).eq(TbSpecParam::getGroupId, specParam.getGroupId()).eq(TbSpecParam::getName, specParam.getName());
        List<TbSpecParam> specParamList = this.list(queryWrapper);
        if (!CollectionUtils.isEmpty(specParamList)) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_PARAM);
        }
        specParam.setCreateTime(new Date());
        specParam.setUpdateTime(new Date());
        boolean result = this.save(specParam);
        if (!result) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    @Override
    public List<SpecGroupDTO> findSpecGroupWithParamListByCategoryId(Long cid) {
        // 查询规格组
        List<SpecGroupDTO> specGroupList = this.findSpecGroupCategoryId(cid);
        // 查询分类下所有规格参数
        List<SpecParamDTO> specParamList = this.findSpecParamByCategoryId(null, cid, null);
        // 将规格参数按照groupId进行分组，得到每个group下的param的集合
        Map<Long, List<SpecParamDTO>> specParamMap = specParamList.stream().collect(Collectors.groupingBy(SpecParamDTO::getGroupId));

/*        specGroupList=specGroupList.stream().map(group->{
            group.setParams(specParamMap.get(group.getId()));
        return group;
        }).collect(Collectors.toList());*/
        // 填写到group中
        for (SpecGroupDTO groupDTO : specGroupList) {
            groupDTO.setParams(specParamMap.get(groupDTO.getId()));
        }
        return specGroupList;
    }

    private List<SpecGroupDTO> findSpecGroupCategoryId(Long cid) {

        QueryWrapper<TbSpecGroup> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TbSpecGroup::getCid, cid);
        List<TbSpecGroup> tbSpecGroupList = specGroupService.list(queryWrapper);
        if (CollectionUtils.isEmpty(tbSpecGroupList)) {
            throw new LyException(ExceptionEnum.SPEC_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(tbSpecGroupList, SpecGroupDTO.class);
    }
}
