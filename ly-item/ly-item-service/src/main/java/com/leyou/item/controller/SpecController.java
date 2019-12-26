package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.entity.TbSpecGroup;
import com.leyou.item.entity.TbSpecParam;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spec")  //规格组和规格参数共用一个
public class SpecController {

    @Autowired
    private TbSpecGroupService specGroupService;

    @Autowired
    private TbSpecParamService specParamService;

    @GetMapping(value = "/groups/of/category", name = "查看分组信息")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCategoryId(@RequestParam("id") Long id) {
        List<SpecGroupDTO> groupDTOList = specGroupService.findSpecGroupByCategoryId(id);
        return ResponseEntity.ok(groupDTOList);
    }

    @PostMapping(value = "/group", name = "保存分组信息")
    public ResponseEntity<List<Void>> saveSpecGroup(@RequestBody TbSpecGroup specGroup) {
        specGroupService.saveSpecGroup(specGroup);
        return ResponseEntity.ok().build();
    }


    @GetMapping(value = "/params", name = "查看详细属性信息")
    public ResponseEntity<List<SpecParamDTO>> findSpecParamByCategoryId(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching) {
        List<SpecParamDTO> paramDTOList = specParamService.findSpecParamByCategoryId(gid, cid, searching);
        return ResponseEntity.ok(paramDTOList);
    }

    @PostMapping(value = "/param", name = "保存参数信息")
    public ResponseEntity<List<Void>> saveSpecParam(@RequestBody TbSpecParam specParam) {
        specParamService.saveSpecParam(specParam);
        return ResponseEntity.ok().build();
    }
}
