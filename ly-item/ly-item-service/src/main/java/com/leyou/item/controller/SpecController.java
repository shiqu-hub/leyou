package com.leyou.item.controller;

import com.leyou.item.dto.SpecGroupDTO;
import com.leyou.item.dto.SpecParamDTO;
import com.leyou.item.service.TbSpecGroupService;
import com.leyou.item.service.TbSpecParamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/spec")  //规格组和规格参数共用一个
public class SpecController {

    @Autowired
    private TbSpecGroupService specGroupService;

    @Autowired
    private TbSpecParamService specParamService;

    @GetMapping(value = "/groups/of/category", name = "")
    public ResponseEntity<List<SpecGroupDTO>> findSpecGroupByCategoryId(@RequestParam("id") Long id) {
        List<SpecGroupDTO> groupDTOList = specGroupService.findSpecGroupByCategoryId(id);
        return ResponseEntity.ok(groupDTOList);
    }

    @GetMapping(value = "/params", name = "")
    public ResponseEntity<List<SpecParamDTO>> findSpecParamByCategoryId(
            @RequestParam(value = "gid",required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching) {
        List<SpecParamDTO> paramDTOList = specParamService.findSpecParamByCategoryId(gid,cid,searching);
        return ResponseEntity.ok(paramDTOList);
    }
}
