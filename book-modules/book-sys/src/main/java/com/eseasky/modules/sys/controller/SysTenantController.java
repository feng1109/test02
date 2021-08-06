package com.eseasky.modules.sys.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.entity.SysTenant;
import com.eseasky.common.service.SysTenantService;
import com.eseasky.modules.sys.dto.SysTenantDTO;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "租户管理")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/systenant")
public class SysTenantController {

    @Autowired
    SysTenantService sysTenantService;

    @ApiOperation(value = "获取租户信息", notes = "获取租户信息")
    @GetMapping("getTenant")
    public R<List<SysTenantDTO>> getTenant() {
        List<SysTenant> list = sysTenantService.list(new QueryWrapper<SysTenant>().lambda().eq(SysTenant::getStatus, "1"));
        List<SysTenantDTO> sysTenantDTOS = Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(list)) {
            list.stream().forEach(s -> {
                SysTenantDTO sysTenantDTO = new SysTenantDTO();
                String tenantCode = s.getTenantCode();
                String tenantName = s.getTenantName();
                sysTenantDTO.setTenantCode(tenantCode);
                sysTenantDTO.setTenantName(tenantName);
                sysTenantDTOS.add(sysTenantDTO);
            });
        }
        return R.ok(sysTenantDTOS);

    }


}
