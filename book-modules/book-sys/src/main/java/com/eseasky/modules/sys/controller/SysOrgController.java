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
 * ????????????
 *
 * @author
 * @date 2021-04-15 09:13:46
 */
@Api(tags = "????????????")
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
     * ????????????????????????
     *
     * @param pageResultVO ????????????????????????
     * @return
     */
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    @PostMapping("/page")
    @Log(value = "????????????????????????")
    public R<IPage<SysOrg>> getSysOrgPage(@RequestBody @Validated PageResultVO<SysOrg> pageResultVO) {
        Page page = new Page(pageResultVO.getCurrent(), pageResultVO.getSize());
        return new R<>(sysOrgService.getOrgPage(page, pageResultVO.getParam()));
    }


    @ApiOperation(value = "?????????????????????????????????", notes = "?????????????????????????????????")
    @GetMapping("/findAllByRole")
    @Log(value = "?????????????????????????????????")
    public R<List<SysOrg>> findAllByRole(SysOrg sysOrg) {
        Set<String> orgIds = getDataScope();
        if(CollectionUtil.isNotEmpty(orgIds)){
            sysOrg.setOrgIds(orgIds);
        }

        List<SysOrg> list = sysOrgService.getOrgList(sysOrg);
        return new R<>(list);
    }


    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @GetMapping("/findAll")
    @Log(value = "??????????????????")
    public R<List<SysOrg>> findAll(SysOrg sysOrg) {
        List<SysOrg> list = sysOrgService.getOrgList(sysOrg);
        return new R<>(list);
    }

    /**
     * ??????id??????????????????
     *
     * @param id
     * @return R
     */
    @ApiOperation(value = "??????id??????????????????", notes = "??????id??????????????????")
    @GetMapping("/{id}")
    @Log(value = "??????id??????????????????")
    public R<SysOrg> getById(@PathVariable("id") String id) {
        return new R<>(sysOrgService.findById(id));
    }

    /**
     * ????????????
     *
     * @param sysOrg
     * @return R
     */
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @PostMapping
    @Log(value = "??????????????????")
    @PreAuthorize("hasAuthority('sys:org:add')")
    public R save(@RequestBody SysOrg sysOrg) {
        sysOrg.setCreateTime(LocalDateTime.now());
        sysOrg.setCreateUser(getUser().getSysUserDTO().getId());
        if (StringUtils.isBlank(sysOrg.getPid())) {
            sysOrg.setPid("-1");
        }
        SysOrg org = sysOrgService.getOne(new QueryWrapper<SysOrg>().lambda().eq(SysOrg::getOrgName, sysOrg.getOrgName()));
        if (org != null) {
            return R.error("?????????" + sysOrg.getOrgName() + "????????????");
        }

        return new R<>(sysOrgService.save(sysOrg));
    }

    /**
     * ????????????
     *
     * @param sysOrg
     * @return R
     */
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @PutMapping
    @Log(value = "??????????????????")
    @PreAuthorize("hasAuthority('sys:org:edit')")
    public R update(@RequestBody SysOrg sysOrg) {
        String pid = sysOrg.getPid();
        String id = sysOrg.getId();
        if (StringUtils.equals(pid, id)) {
            return R.error("?????????????????????????????????");
        }
        sysOrg.setUpdateTime(LocalDateTime.now());
        sysOrg.setUpdateUser(getUser().getSysUserDTO().getId());
        SysOrg sysorgByid = sysOrgService.getById(sysOrg.getId());
        if (!StringUtils.equals(sysorgByid.getOrgName().trim(), sysOrg.getOrgName().trim())) {
            SysOrg org = sysOrgService.getOne(new QueryWrapper<SysOrg>().lambda().eq(SysOrg::getOrgName, sysOrg.getOrgName().trim()));
            if (org != null) {
                return R.error("?????????" + sysOrg.getOrgName() + "????????????");
            }
        }


        return new R<>(sysOrgService.updateById(sysOrg));
    }

    /**
     * ??????id??????????????????
     * @param id
     * @return R
     */
   /* @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @DeleteMapping("/{id}")
    public R removeById(@PathVariable String id) {
        sysOrgService.lambdaUpdate().set(SysOrg::getDelFlag,DEL).eq(SysOrg::getId,id);
        return R.ok();
    }*/


    /**
     * ????????????
     *
     * @param ids (array)
     * @return R
     */
    @ApiOperation(value = "????????????????????????", notes = "????????????????????????")
    @DeleteMapping("/deleteBatch")
    @Log(value = "????????????????????????")
    @PreAuthorize("hasAuthority('sys:org:delete')")
    public R deleteBatch(String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return R.error("????????????????????????!");
        } else {
            List<String> list = JSONUtil.parseArray(StringUtils.splitPreserveAllTokens(ids, ",")).toList(String.class);
            orgCascadeHandleService.deleteOrgCascade(list);
            return R.ok("????????????");
        }
    }


    /**
     * ????????????
     */
    @GetMapping({"/exportExcel"})
    @ApiOperation(value = "??????????????????", notes = "??????????????????")
    @Log(value = "??????????????????")
    public void exportXls(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<OrgExcelVO> list = Lists.newArrayList();
        ExportParams exportParams = new ExportParams();
        exportParams.setCreateHeadRows(false);
        exportExcel(list, OrgExcelVO.class, "????????????", new ExportParams(null, null, "????????????"), response);
    }


    @ApiOperation(value = "????????????", notes = "????????????")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @Log(value = "????????????")
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
                return R.error("??????????????????");
            }
            List<SysOrg> collect = sysOrgExcels.stream().filter(s -> StringUtils.isNotBlank(s.getOrgName())).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(collect)) {
                return R.error("????????????????????????");
            }
            //??????
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
                return R.ok("????????????????????????????????????" + collect.size());
            }

            return R.ok("????????????????????????????????????" + 0);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error("??????????????????:" + e.getCause() + "," + e.getMessage());
        } finally {
            try {
                file.getInputStream().close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

/*    @ApiOperation(value = "????????????", notes = "????????????")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    @Log(value = "????????????")
    public R importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        ImportParams params = new ImportParams();
        params.setTitleRows(0);
        params.setNeedSave(false);
        try {
            List<SysOrg> sysOrgExcels = ExcelImportUtil.importExcel(file.getInputStream(), SysOrg.class, params);
            if (CollectionUtil.isEmpty(sysOrgExcels)) {
                return R.error("??????????????????");
            }
            List<SysOrg> collect = sysOrgExcels.stream().filter(s -> StringUtils.isNotBlank(s.getOrgName())).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(collect)) {
                return R.error("????????????????????????");
            }
            //??????
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
                return R.ok("????????????????????????????????????" + collect.size());
            }

            return R.ok("????????????????????????????????????" + 0);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.error("??????????????????:" + e.getCause() + "," + e.getMessage());
        } finally {
            try {
                file.getInputStream().close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }*/








}
