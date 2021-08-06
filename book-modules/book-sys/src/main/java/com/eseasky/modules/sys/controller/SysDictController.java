package com.eseasky.modules.sys.controller;


import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.entity.SysDict;
import com.eseasky.common.service.SysDictItemService;
import com.eseasky.common.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Api(value = "字典管理", tags = "字典管理")
@RestController
@RequestMapping("/sysDict")
public class SysDictController {

    @Autowired
    SysDictService sysDictService;

    @Autowired
    SysDictItemService sysDictItemService;

    @ApiOperation(value = "通过code查找字典", notes = "通过code查找字典")
    @GetMapping("findByCode")
    @Log(value = "通过code查找字典")
    public R<List<SysDict>> findByCode(@RequestParam(required = true) String code) {
        return new R(sysDictItemService.findByCode(code));
    }


}
