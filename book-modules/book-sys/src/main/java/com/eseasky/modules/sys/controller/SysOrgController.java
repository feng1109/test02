package com.eseasky.modules.sys.controller;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.dto.OrgDTO;
import com.eseasky.common.code.dto.SysUserDTO;
import com.eseasky.common.code.entity.UserDetailsImpl;
import com.eseasky.common.code.utils.*;
import com.eseasky.common.code.vo.PageResultVO;
import com.eseasky.common.entity.SysOrg;
import com.eseasky.common.service.SysOrgService;
import com.eseasky.common.service.utils.OrgTree;
import com.eseasky.modules.sys.service.OrgCascadeHandleService;
import com.eseasky.modules.sys.vo.OrgExcelVO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.org.apache.xpath.internal.SourceTree;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.Buffer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.eseasky.common.code.fun.StreamUtils.distinctByKey;
import static com.eseasky.common.code.utils.ExcelUtils.exportExcel;
import static com.eseasky.common.code.utils.SecurityUtils.getDataScope;
import static com.eseasky.common.code.utils.SecurityUtils.getUser;


/**
 * 组织管理
 *
 * @author
 * @date 2021-04-15 09:13:46
 */
@Api(tags = "组织管理")
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/sysorg")
public class SysOrgController {

    @Autowired
    private SysOrgService sysOrgService;

    @Autowired
    OrgCascadeHandleService orgCascadeHandleService;


    /**
     * 组织管理分页查询
     *
     * @param pageResultVO 组织管理分页对象
     * @return
     */
    @ApiOperation(value = "组织管理分页查询", notes = "组织管理分页查询")
    @PostMapping("/page")
    @Log(value = "组织管理分页查询")
    public R<IPage<SysOrg>> getSysOrgPage(@RequestBody @Validated PageResultVO<SysOrg> pageResultVO) {
        Page page = new Page(pageResultVO.getCurrent(), pageResultVO.getSize());
        return new R<>(sysOrgService.getOrgPage(page, pageResultVO.getParam()));
    }


    @ApiOperation(value = "查询角色对应的组织组织", notes = "查询角色对应的组织组织")
    @GetMapping("/findAllByRole")
    @Log(value = "查询角色对应的组织组织")
    public R<List<SysOrg>> findAllByRole(SysOrg sysOrg) {
        Set<String> orgIds = getDataScope();
        if(CollectionUtil.isNotEmpty(orgIds)){
            sysOrg.setOrgIds(orgIds);
        }

        List<SysOrg> list = sysOrgService.getOrgList(sysOrg);
        return new R<>(list);
    }


    @ApiOperation(value = "查询所有组织", notes = "查询所有组织")
    @GetMapping("/findAll")
    @Log(value = "查询所有组织")
    public R<List<SysOrg>> findAll(SysOrg sysOrg) {
        List<SysOrg> list = sysOrgService.getOrgList(sysOrg);
        return new R<>(list);
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
    public R<SysOrg> getById(@PathVariable("id") String id) {
        return new R<>(sysOrgService.findById(id));
    }

    /**
     * 新增记录
     *
     * @param sysOrg
     * @return R
     */
    @ApiOperation(value = "新增组织管理", notes = "新增组织管理")
    @PostMapping
    @Log(value = "新增组织管理")
    @PreAuthorize("hasAuthority('sys:org:add')")
    public R save(@RequestBody SysOrg sysOrg) {
        sysOrg.setCreateTime(LocalDateTime.now());
        sysOrg.setCreateUser(getUser().getSysUserDTO().getId());
        if (StringUtils.isBlank(sysOrg.getPid())) {
            sysOrg.setPid("-1");
        }
        SysOrg org = sysOrgService.getOne(new QueryWrapper<SysOrg>().lambda().eq(SysOrg::getOrgName, sysOrg.getOrgName()));
        if (org != null) {
            return R.error("组织【" + sysOrg.getOrgName() + "】已存在");
        }

        return new R<>(sysOrgService.save(sysOrg));
    }

    /**
     * 修改记录
     *
     * @param sysOrg
     * @return R
     */
    @ApiOperation(value = "修改组织管理", notes = "修改组织管理")
    @PutMapping
    @Log(value = "修改组织管理")
    @PreAuthorize("hasAuthority('sys:org:edit')")
    public R update(@RequestBody SysOrg sysOrg) {
        String pid = sysOrg.getPid();
        String id = sysOrg.getId();
        if (StringUtils.equals(pid, id)) {
            return R.error("当前部门不能为上级部门");
        }
        sysOrg.setUpdateTime(LocalDateTime.now());
        sysOrg.setUpdateUser(getUser().getSysUserDTO().getId());
        SysOrg sysorgByid = sysOrgService.getById(sysOrg.getId());
        if (!StringUtils.equals(sysorgByid.getOrgName().trim(), sysOrg.getOrgName().trim())) {
            SysOrg org = sysOrgService.getOne(new QueryWrapper<SysOrg>().lambda().eq(SysOrg::getOrgName, sysOrg.getOrgName().trim()));
            if (org != null) {
                return R.error("组织【" + sysOrg.getOrgName() + "】已存在");
            }
        }


        return new R<>(sysOrgService.updateById(sysOrg));
    }

    /**
     * 通过id删除一条记录
     * @param id
     * @return R
     */
   /* @ApiOperation(value = "删除组织管理", notes = "删除组织管理")
    @DeleteMapping("/{id}")
    public R removeById(@PathVariable String id) {
        sysOrgService.lambdaUpdate().set(SysOrg::getDelFlag,DEL).eq(SysOrg::getId,id);
        return R.ok();
    }*/


    /**
     * 批量删除
     *
     * @param ids (array)
     * @return R
     */
    @ApiOperation(value = "批量删除组织管理", notes = "批量删除组织管理")
    @DeleteMapping("/deleteBatch")
    @Log(value = "批量删除组织管理")
    @PreAuthorize("hasAuthority('sys:org:delete')")
    public R deleteBatch(String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return R.error("删除数据不能为空!");
        } else {
            List<String> list = JSONUtil.parseArray(StringUtils.splitPreserveAllTokens(ids, ",")).toList(String.class);
            orgCascadeHandleService.deleteOrgCascade(list);
            return R.ok("删除成功");
        }
    }


    /**
     * 模板导出
     */
    @GetMapping({"/exportExcel"})
    @ApiOperation(value = "部门模板导出", notes = "部门模板导出")
    @Log(value = "部门模板导出")
    public void exportXls(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<OrgExcelVO> list = Lists.newArrayList();
        ExportParams exportParams = new ExportParams();
        exportParams.setCreateHeadRows(false);
        exportExcel(list, OrgExcelVO.class, "部门模板", new ExportParams(null, null, "部门信息"), response);
    }


    @ApiOperation(value = "导入部门", notes = "导入部门")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @Log(value = "导入部门")
    @PreAuthorize("hasAuthority('sys:org:import')")
    public R importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        ImportParams params = new ImportParams();
        params.setTitleRows(0);
        params.setNeedSave(false);
        try {
            List<SysOrg> sysOrgExcels = ExcelImportUtil.importExcel(file.getInputStream(), SysOrg.class, params);
            if (CollectionUtil.isEmpty(sysOrgExcels)) {
                return R.error("部门不能为空");
            }
            List<SysOrg> collect = sysOrgExcels.stream().filter(s -> StringUtils.isNotBlank(s.getOrgName())).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(collect)) {
                return R.error("模板数据不能为空");
            }
            //去重
            List<SysOrg> orgList = collect.stream().filter(distinctByKey(s -> s.getOrgName().trim())).collect(Collectors.toList());
            List<SysOrg> list = sysOrgService.list();
            List<SysOrg> result = null;
            if (CollectionUtil.isNotEmpty(list)) {
                List<String> orgnames = list.stream().map(o -> o.getOrgName()).collect(Collectors.toList());

                List<SysOrg> newOrgs = orgList.stream().filter(s -> !orgnames.contains(s.getOrgName())).collect(Collectors.toList());
                if (CollectionUtil.isNotEmpty(newOrgs)) {
                    list.stream().forEach(s -> {
                        newOrgs.stream().forEach(o -> {
                            if (StringUtils.equals(s.getOrgName().trim(), o.getParentName().trim())) {
                                o.setPid(s.getId());
                                o.setId(IdUtil.simpleUUID());
                            }
                        });
                    });
                    new OrgTree(newOrgs).setNodePid(newOrgs);
                    result = newOrgs;
                }

            } else {
                new OrgTree(orgList).setNodePid(orgList);
                result = orgList;
            }


            SysUserDTO sysUserDTO = getUser().getSysUserDTO();
            if (CollectionUtil.isNotEmpty(result)) {
                result.stream().forEach(s -> {
                    s.setCreateUser(sysUserDTO.getId());
                    s.setCreateTime(LocalDateTime.now());
                });
                sysOrgService.saveBatch(result);
                return R.ok("文件导入成功！数据行数：" + collect.size());
            }

            return R.ok("文件导入成功！数据行数：" + 0);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error("文件导入失败:" + e.getCause() + "," + e.getMessage());
        } finally {
            try {
                file.getInputStream().close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

/*    @ApiOperation(value = "导入部门", notes = "导入部门")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @Log(value = "导入部门")
    public R importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        ImportParams params = new ImportParams();
        params.setTitleRows(0);
        params.setNeedSave(false);
        try {
            List<SysOrg> sysOrgExcels = ExcelImportUtil.importExcel(file.getInputStream(), SysOrg.class, params);
            if (CollectionUtil.isEmpty(sysOrgExcels)) {
                return R.error("部门不能为空");
            }
            List<SysOrg> collect = sysOrgExcels.stream().filter(s -> StringUtils.isNotBlank(s.getOrgName())).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(collect)) {
                return R.error("模板数据不能为空");
            }
            //去重
            List<SysOrg> orgList = collect.stream().filter(distinctByKey(s -> s.getOrgName().trim())).collect(Collectors.toList());
            List<SysOrg> list = sysOrgService.list();


            List<SysOrg> result = null;
            if (CollectionUtil.isNotEmpty(list)) {
                Map<String, SysOrg> treeNodeMap = Maps.newHashMap();
                new OrgTree(list).buildNodechain(treeNodeMap);

                Map<String, SysOrg> excelOrgMap = Maps.newHashMap();
                List<String> orgNames = orgList.stream().map(s -> s.getOrgName()).collect(Collectors.toList());
                List<SysOrg> sysOrgs = bulidNode(orgNames, excelOrgMap);





            } else {
                new OrgTree(orgList).setNodePid(orgList);
                result = orgList;
            }


            SysUserDTO sysUserDTO = getUser().getSysUserDTO();
            if (CollectionUtil.isNotEmpty(result)) {
                assert result != null;
                result.stream().forEach(s -> {
                    s.setCreateUser(sysUserDTO.getId());
                    s.setCreateTime(LocalDateTime.now());
                });
                sysOrgService.saveBatch(result);
                return R.ok("文件导入成功！数据行数：" + collect.size());
            }

            return R.ok("文件导入成功！数据行数：" + 0);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error("文件导入失败:" + e.getCause() + "," + e.getMessage());
        } finally {
            try {
                file.getInputStream().close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }*/








}
