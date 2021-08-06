package com.eseasky.modules.sys.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.entity.SysResource;
import com.eseasky.common.service.SysResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.eseasky.common.code.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import com.eseasky.common.code.vo.PageResultVO;

import java.util.List;


/**
 * 菜单资源表
 *
 * @author
 * @date 2021-04-19 15:30:04
 */
@Api(tags = "菜单资源表")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/sysresource")
public class SysResourceController {

    @Autowired
    private SysResourceService sysResourceService;

    /**
     * 菜单资源表分页查询
     *
     * @param pageResultVO 菜单资源表分页对象
     * @return
     */
    @ApiOperation(value = "菜单资源表分页查询", notes = "菜单资源表分页查询")
    @PostMapping("/page")
    @Log(value = "菜单资源表分页查询")
    public R<IPage<SysResource>> getSysResourcePage(@RequestBody @Validated PageResultVO<SysResource> pageResultVO) {
        Page page = new Page(pageResultVO.getCurrent(), pageResultVO.getSize());
        QueryWrapper<SysResource> queryWrapper = new QueryWrapper<>(pageResultVO.getParam());
        return new R<>(sysResourceService.page(page, queryWrapper));
    }

    @ApiOperation(value = "查看所有的资源", notes = "查看所有的资源")
    @GetMapping("findAll")
    @Log(value = "查看所有的资源")
    public R<List<SysResource>> findAll() {
        return new R(sysResourceService.list(new QueryWrapper<SysResource>().lambda().orderByAsc(SysResource::getSort)));
    }


    /**
     * 通过id查询单条记录
     *
     * @param id
     * @return R
     */
    @ApiOperation(value = "通过id查询单条记录", notes = "通过id查询单条记录")
    @GetMapping("/{id}")
    @Log(value = "通过id查询单条记录")
    public R<SysResource> getById(@PathVariable("id") String id) {
        return new R<>(sysResourceService.getById(id));
    }

    /**
     * 新增记录
     *
     * @param sysResource
     * @return R
     */
    @ApiOperation(value = "新增菜单资源表", notes = "新增菜单资源表")
    @PostMapping
    @Log(value = "新增菜单资源表")
    public R save(@RequestBody SysResource sysResource) {
        return new R<>(sysResourceService.save(sysResource));
    }

    /**
     * 修改记录
     *
     * @param sysResource
     * @return R
     */
    @ApiOperation(value = "修改菜单资源表", notes = "修改菜单资源表")
    @PutMapping
    @Log(value = "修改菜单资源表")
    public R update(@RequestBody SysResource sysResource) {
        return new R<>(sysResourceService.updateById(sysResource));
    }

    /**
     * 通过id删除一条记录
     *
     * @param id
     * @return R
     */
    @ApiOperation(value = "删除菜单资源表", notes = "删除菜单资源表")
    @DeleteMapping("/{id}")
    @Log(value = "删除菜单资源表")
    public R removeById(@PathVariable String id) {
        return new R<>(sysResourceService.removeById(id));
    }

    /**
     * 批量删除
     *
     * @param ids (array)
     * @return R
     */
    @ApiOperation(value = "批量删除菜单资源表", notes = "批量删除菜单资源表")
    @DeleteMapping("/deleteBatch")
    @Log(value = "批量删除菜单资源表")
    public R deleteBatch(@RequestBody String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return R.error("删除数据不能为空!");
        } else {
            this.sysResourceService.removeByIds(JSONUtil.parseArray(ids).toList(String.class));
            return R.ok("删除成功");
        }
    }


}
