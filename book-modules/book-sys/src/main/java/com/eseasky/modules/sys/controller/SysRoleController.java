package com.eseasky.modules.sys.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.sys.vo.RoleResourceVO;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.vo.PageResultVO;
import com.eseasky.common.entity.SysRole;
import com.eseasky.common.entity.SysRoleOrg;
import com.eseasky.common.entity.SysUser;
import com.eseasky.common.entity.SysUserRole;
import com.eseasky.common.service.SysRoleOrgService;
import com.eseasky.common.service.SysRoleService;
import com.eseasky.common.service.SysUserRoleService;
import com.eseasky.modules.sys.vo.SysRoleVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.eseasky.common.code.utils.SecurityUtils.getDataScope;


/**
 * 角色表
 *
 * @author
 * @date 2021-04-15 13:38:31
 */
@Api(tags = "角色表")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/sysrole")
public class SysRoleController {

    @Autowired
    SysRoleService sysRoleService;

    @Autowired
    SysUserRoleService sysUserRoleService;

    @Autowired
    SysRoleOrgService sysRoleOrgService;

    /**
     * 角色表分页查询
     *
     * @param pageResultVO 角色表分页对象
     * @return
     */
    @ApiOperation(value = "角色表分页查询", notes = "角色表分页查询")
    @PostMapping("/page")
    @Log(value = "角色表分页查询")
    public R<IPage<SysRole>> getSysRolePage(@RequestBody @Validated PageResultVO<SysRole> pageResultVO) {
        SysRole param = pageResultVO.getParam();
        Set<String> OrgIds = getDataScope();
        if(CollectionUtil.isNotEmpty(OrgIds)){
            List<SysRoleOrg> sysRoleOrgs = sysRoleOrgService.list(new QueryWrapper<SysRoleOrg>().lambda().in(SysRoleOrg::getOrgId, OrgIds));
            if(CollectionUtil.isNotEmpty(sysRoleOrgs)){
                Set<String> collect = sysRoleOrgs.stream().map(s -> s.getRoleId()).collect(Collectors.toSet());
                param.setRoleIds(collect);
            }
            param.setOrgIds(OrgIds);
        }



        Page page = new Page(pageResultVO.getCurrent(), pageResultVO.getSize());
        IPage<SysRole> rolePage = sysRoleService.getRolePage(page, param);
        return new R<>(rolePage);
    }





    /**
     * 通过角色id查找人员
     *
     * @param pageResultVO 通过角色id查找人员
     * @return
     */
    @ApiOperation(value = "通过角色id查找人员", notes = "通过角色id查找人员")
    @PostMapping("/findByRoleIdPage")
    @Log(value = "通过角色id查找人员")
    public R<IPage<SysUser>> findByRoleIdPage(@RequestBody @Validated PageResultVO<SysUser> pageResultVO) {
        Page page = new Page(pageResultVO.getCurrent(), pageResultVO.getSize());
        IPage<SysUser> rolePage = sysRoleService.findByRoleId(page, pageResultVO.getParam());
        return new R<>(rolePage);
    }



    @ApiOperation(value = "获取所有角色", notes = "获取所有角色")
    @GetMapping("/findAll")
    @Log(value = "获取所有角色")
    public R<List<SysRole>> findAll() {
        SysRole param = new SysRole();
        Set<String> OrgIds = getDataScope();
        if(CollectionUtil.isNotEmpty(OrgIds)){
            List<SysRoleOrg> sysRoleOrgs = sysRoleOrgService.list(new QueryWrapper<SysRoleOrg>().lambda().in(SysRoleOrg::getOrgId, OrgIds));
            if(CollectionUtil.isNotEmpty(sysRoleOrgs)){
                Set<String> collect = sysRoleOrgs.stream().map(s -> s.getRoleId()).collect(Collectors.toSet());
                param.setRoleIds(collect);
            }
            param.setOrgIds(OrgIds);
        }
        return new R<>(sysRoleService.getRole(param));
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
    public R<SysRole> getById(@PathVariable("id") String id) {
        return new R<>(sysRoleService.getRoleById(id));
    }




    /**
     * 新增记录
     *
     * @param sysRole
     * @return R
     */
    @ApiOperation(value = "新增角色表", notes = "新增角色表")
    @PostMapping("save")
    @Log(value = "新增角色")
    @PreAuthorize("hasAuthority('sys:role:add')")
    public R save(@RequestBody SysRole sysRole) {
        sysRoleService.addRole(sysRole);
        return new R<>();
    }




    /**
     * 修改记录
     *
     * @param sysRole
     * @return R
     */
    @ApiOperation(value = "修改角色表", notes = "修改角色表")
    @PutMapping("update")
    @Log(value = "修改角色")
    @PreAuthorize("hasAuthority('sys:role:edit')")
    public R update(@RequestBody SysRole sysRole) {
        sysRoleService.editRole(sysRole);
        return R.ok();
    }





    /**
     * 通过id删除一条记录
     *
     * @param id
     * @return R
     */
    @ApiOperation(value = "删除角色表", notes = "删除角色表")
    @DeleteMapping("deleteRole/{id}")
    @Log(value = "删除角色表")
    @PreAuthorize("hasAuthority('sys:role:delete')")
    public R removeById(@PathVariable String id) {
        List<SysUserRole> list = sysUserRoleService.list(new QueryWrapper<SysUserRole>().lambda().eq(SysUserRole::getRoleId, id));
        if (CollectionUtil.isNotEmpty(list)) {
            return R.error("该角色已绑定用户");
        }
        sysRoleService.deleteRole(id);
        return R.ok();
    }



    /**
     * 批量删除
     *
     * @param ids (array)
     * @return R
     */
    @ApiOperation(value = "批量删除角色表", notes = "批量删除角色表")
    @PostMapping("/deleteBatch")
    @Log(value = "批量删除角色")
    public R deleteBatch(String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return R.error("删除数据不能为空!");
        } else {
            this.sysRoleService.deleteBatch(ids);
            return R.ok("删除成功");
        }
    }



    /**
     * 移除角色人员
     *
     * @param sysRoleVO
     * @return
     */
    @ApiOperation(value = "移除人员", notes = "移除人员")
    @PostMapping("deleteRoleUser")
    @Log(value = "移除人员")
    public R deleteRoleUser(@RequestBody @Validated SysRoleVO sysRoleVO) {
        return sysRoleService.deleteRoleUser(sysRoleVO.getRoleId(), sysRoleVO.getUserIds());
    }



    /**
     * 添加角色人员
     *
     * @param sysRoleVO
     * @return
     */
    @ApiOperation(value = "添加角色人员", notes = "添加角色人员")
    @PostMapping("saveRoeUser")
    @Log(value = "添加角色")
    @PreAuthorize("hasAuthority('sys:role:addUser')")
    public R saveRoeUser(@RequestBody @Validated SysRoleVO sysRoleVO) {
        return sysRoleService.saveRoeUser(sysRoleVO.getRoleId(), sysRoleVO.getUserIds());
    }



    /**
     * 给角色分配权限
     *
     * @param roleResourceVO
     * @return
     */
    @ApiOperation(value = "给角色分配权限", notes = "给角色分配权限")
    @PostMapping("addPermission")
    @Log(value = "给角色分配权限")
    public R addPermission(@RequestBody @Validated RoleResourceVO roleResourceVO) {
        return sysRoleService.addPermission(roleResourceVO);
    }




    /**
     * 编辑角色权限
     *
     * @param roleResourceVO
     * @return
     */
    @ApiOperation(value = "编辑角色权限", notes = "编辑角色权限")
    @PutMapping("addPermission")
    @Log(value = "编辑角色权限")
    @PreAuthorize("hasAuthority('sys:role:setting')")
    public R editPermission(@RequestBody @Validated RoleResourceVO roleResourceVO) {
        return sysRoleService.editPermission(roleResourceVO);
    }



    /**
     * 获取角色对应的权限
     *
     * @param roleId
     * @return
     */
    @ApiOperation(value = "获取角色对应的权限", notes = "获取角色对应的权限")
    @GetMapping("getRolePermission")
    @Log(value = "获取角色对应的权限")
    public R getRolePermission(@RequestParam(value = "roleId", required = true) String roleId) {
        return R.ok(sysRoleService.getRolePermission(roleId));
    }

}
