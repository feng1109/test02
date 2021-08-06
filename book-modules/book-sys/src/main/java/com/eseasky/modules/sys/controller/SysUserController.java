package com.eseasky.modules.sys.controller;

import static com.eseasky.common.code.utils.ExcelUtils.buildExcelDocument;
import static com.eseasky.common.code.utils.ExcelUtils.buildExcelFile;
import static com.eseasky.common.code.utils.ExcelUtils.createTitle;
import static com.eseasky.common.code.utils.ExcelUtils.setHSSFValidation;
import static com.eseasky.common.code.utils.SecurityUtils.getDataScope;
import static com.eseasky.common.code.utils.SecurityUtils.getUser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.common.code.annotations.Log;
import com.eseasky.common.code.dto.OrgDTO;
import com.eseasky.common.code.entity.UserDetailsImpl;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.code.vo.PageResultVO;
import com.eseasky.common.entity.SysDictItem;
import com.eseasky.common.entity.SysOrg;
import com.eseasky.common.entity.SysUser;
import com.eseasky.common.service.SysDictItemService;
import com.eseasky.common.service.SysOrgService;
import com.eseasky.common.service.SysUserService;
import com.eseasky.datasource.config.DynamicDataSourceContextHolder;
import com.eseasky.modules.sys.vo.ChangePasswdVO;
import com.eseasky.modules.sys.vo.ForgetPasswdVO;
import com.eseasky.modules.sys.vo.SysUserVO;
import com.google.common.collect.Lists;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Api(value = "用户管理", tags = "用户管理")
@RestController
@RequestMapping("/sysUser")
public class SysUserController {

    @Autowired
    SysUserService sysUserService;

    @Autowired
    SysDictItemService sysDictItemService;

    @Autowired
    SysOrgService sysOrgService;


    /**
     * 用户表分页查询
     *
     * @param pageResultVO 用户表分页查询
     * @return
     */
    @ApiOperation(value = "用户表分页查询", notes = "用户表分页查询")
    @PostMapping("/page")
    @Log(value = "用户表分页查询")
    public R<IPage<SysUser>> getSysRolePage(@RequestBody @Validated PageResultVO<SysUser> pageResultVO) {
        Page page = new Page(pageResultVO.getCurrent(), pageResultVO.getSize());
        Set<String> orgIds = getDataScope();
        SysUser param = pageResultVO.getParam();
        Set<String> orgIdsVo = param.getOrgIds();
        if (CollectionUtil.isEmpty(orgIdsVo) && CollectionUtil.isNotEmpty(orgIds)) {
            param.setOrgIds(orgIds);
        }
        IPage<SysUser> rolePage = sysUserService.getUserPage(page, param);
        List<SysUser> records = rolePage.getRecords();
        if (CollectionUtil.isNotEmpty(records)) {
            List<SysDictItem> duty = sysDictItemService.findByCode("duty");
            Map<String, SysDictItem> dictMap = duty.stream().collect(Collectors.toMap(SysDictItem::getItemValue, d -> d));
            records.stream().forEach(s -> {
                String userType = s.getUserType();
                if (dictMap.containsKey(userType)) {
                    s.setUserType(dictMap.get(userType).getItemText());
                }
            });
        }

        return new R<>(rolePage);
    }


    @ApiOperation(value = "指定人员规则使用人员", notes = "指定人员规则使用人员")
    @GetMapping("/getRuleUser")
    @Log(value = "指定人员规则使用人员")
    public R<List<SysUser>> getRuleUser() {
        SysUser sysUser = new SysUser();
        Set<String> orgIds = getDataScope();
        if (CollectionUtil.isNotEmpty(orgIds)) {
            sysUser.setOrgIds(orgIds);
        }
        List<SysUser> result = sysUserService.getRuleUser(sysUser);
        return new R<>(result);
    }


    /**
     * 新增用户
     *
     * @param sysUser
     * @return
     */
    @ApiOperation(value = "新增用户", notes = "新增用户")
    @PostMapping("add")
    // @PreAuthorize("hasAuthority('sys:user:add')")
    @Log(value = "新增用户")
    public R add(@RequestBody @Validated SysUser sysUser) {
        return sysUserService.add(sysUser);
    }


    /**
     * 编辑用户
     *
     * @param sysUser
     * @return
     */
    @ApiOperation(value = "编辑用户", notes = "编辑用户")
    @PutMapping("edit")
    // @PreAuthorize("hasAuthority('sys:user:edit')")
    @Log(value = "编辑用户")
    public R edit(@RequestBody @Validated SysUser sysUser) {
        sysUserService.edit(sysUser);
        return R.ok();
    }

    @ApiOperation(value = "通过id获取用户", notes = "通过id获取用户")
    @GetMapping("getById")
    @Log(value = "通过id获取用户")
    public R<SysUser> getById(@RequestParam(required = true) String id) {
        SysUser sysUser = sysUserService.findById(id);
        return new R(sysUser);
    }


    @ApiOperation(value = "批量修改部门", notes = "通过id获取用户")
    @PutMapping("updateOrgBatch")
    @Log(value = "通过id获取用户")
    public R updateOrgBatch(@RequestBody SysUserVO sysUserVO) {
        sysUserService.updateOrgBatch(sysUserVO.getUserIds(), sysUserVO.getOrgIds());
        return R.ok();
    }


    /**
     * 通过id删除用户
     *
     * @param id
     * @return
     */
    @ApiOperation(value = "删除用户", notes = "删除用户")
    @DeleteMapping("delete/{id}")
    // @PreAuthorize("hasAuthority('sys:user:delete')")
    @Log(value = "删除用户")
    public R delete(@PathVariable("id") String id) {
        SysUser user = sysUserService.getById(id);
        if (user != null && StringUtils.equals(user.getUserType(), "0")) {
            return R.error("超级管理员账户不能删除");
        }
        return sysUserService.delete(id);
    }


    @Log(value = "修改密码")
    @ApiOperation(value = "修改密码", notes = "修改密码")
    @PostMapping(value = "changePassword", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<String> changePassword(@RequestBody @Valid ChangePasswdVO param) {

        String oldPassword = param.getOldPassword();
        String newPassword = param.getNewPassword();
        String confirmPassword = param.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            return R.error("新密码不一致！");
        }


        UserDetailsImpl user = getUser();
        SysUser oldSysUser = sysUserService.getById(user.getSysUserDTO().getId());
        String oldPasswordToComp = oldSysUser.getPassword();

        String encodeOldPassword = DigestUtil.md5Hex(oldPassword);
        if (!StringUtils.equals(encodeOldPassword, oldPasswordToComp)) {
            return R.error("旧密码不正确！");
        }

        sysUserService.update(new UpdateWrapper<SysUser>() //
                .set("password", DigestUtil.md5Hex(confirmPassword)) //
                .set("update_user", user.getSysUserDTO().getId()) //
                .set("update_time", LocalDateTime.now()) //
                .eq("id", user.getSysUserDTO().getId()));

        return R.ok();
    }

    @ApiOperation(value = "忘记密码", notes = "忘记密码")
    @PostMapping(value = "forgetPassword", consumes = MediaType.APPLICATION_JSON_VALUE)
    public R<String> forgetPassword(@RequestBody @Valid ForgetPasswdVO param) {
        String tenant = "whu";
        String mobile = param.getMobile();
        String newPassword = param.getNewPassword();
        String confirmPassword = param.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            return R.error("新密码不一致！");
        }


        try {
            DynamicDataSourceContextHolder.setDataSourceKey(tenant);
            SysUser sysUser = sysUserService.findByMobile(mobile);
            if (sysUser == null) {
                return R.error("该手机号还未注册！");
            }

            sysUserService.update(new UpdateWrapper<SysUser>() //
                    .set("password", DigestUtil.md5Hex(confirmPassword)) //
                    .set("update_user", sysUser.getId()) //
                    .set("update_time", LocalDateTime.now()) //
                    .eq("id", sysUser.getId()));

        } catch (Exception e) {
            log.error("SysUserController-forgetPassword，忘记并修改密码失败，e：{}", e);
            return R.error("修改密码失败，请联系管理员！");
        } finally {
            DynamicDataSourceContextHolder.clearDataSourceKey();
        }
        return R.ok();
    }


    /**
     * 批量删除
     *
     * @param ids (array)
     * @return R
     */
    @ApiOperation(value = "批量删除用户（参数ids:[1,2]）", notes = "批量删除用户（参数ids:[1,2]）")
    @DeleteMapping("/deleteBatch")
    @Log(value = "批量删除用户")
    public R deleteBatch(String ids) {
        if (ids == null || "".equals(ids.trim())) {
            return R.error("删除数据不能为空!");
        } else {
            this.sysUserService.deleteBatch(ids);
            return R.ok("删除成功");
        }
    }

    /**
     * 模板导出
     */
    @PostMapping({"/exportExcel"})
    @ApiOperation(value = "用户模板导出", notes = "用户模板导出")
    // @PreAuthorize("hasAuthority('sys:user:exportXls')")
    @Log(value = "用户模板导出")
    public void exportXls(HttpServletRequest request, HttpServletResponse response) {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("用户信息");
        List<String> titiles = Lists.newArrayList(new String[] {"用户名", "手机号码", "人员类型", "职位", "用户编号", "学历"});
        createTitle(wb, sheet, 0, titiles);
        String fileName = "人员管理模板.xls";

        try {

            List<SysDictItem> educations = sysDictItemService.findByCode("education");

            String[] educationArr = new String[educations.size()];
            for (int i = 0; i < educationArr.length; i++) {
                educationArr[i] = educations.get(i).getItemValue() + "_" + educations.get(i).getItemText();
            }

            // 设置学历
            setHSSFValidation(sheet, wb, "education", educationArr, 1, 5);

            // 设置人员类型
            String[] userTypeArr = new String[2];
            userTypeArr[0] = "1_学生";
            userTypeArr[1] = "2_教职工";
            setHSSFValidation(sheet, wb, "userType", userTypeArr, 1, 2);


            buildExcelFile(fileName, wb);
            buildExcelDocument(fileName, wb, response);
        } catch (Exception var12) {
            log.error("导出用户模板{}{}", var12);
        }

    }


    @ApiOperation(value = "导入人员", notes = "导入人员")
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    // @PreAuthorize("hasAuthority('sys:user:importExcel')")
    @Log(value = "导入人员")
    public R importExcel(HttpServletRequest request, HttpServletResponse response) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile file = multipartRequest.getFile("file");
        ImportParams params = new ImportParams();
        params.setTitleRows(0);
        params.setNeedSave(false);
        try {
            List<SysUser> sysUserList = ExcelImportUtil.importExcel(file.getInputStream(), SysUser.class, params);
            if (CollectionUtil.isEmpty(sysUserList)) {
                return R.error("导入的人员不能为空");
            }

            List<SysUser> users = sysUserService.list();
            List<SysUser> collect = sysUserList.stream()
                    .filter(s -> StringUtils.isNotBlank(s.getUserNo()) && StringUtils.isNotBlank(s.getPhone()) && PhoneUtil.isMobile(s.getPhone()))
                    .collect(Collectors.toList());
            if (CollectionUtil.isEmpty(collect)) {
                return R.error("手机号码和学号必填");
            }

            List<String> userNos = collect.stream().map(s -> s.getUserNo()).collect(Collectors.toList());
            List<String> phones = collect.stream().map(s -> s.getPhone()).collect(Collectors.toList());


            List<String> dbUserNos = users.stream().map(s -> s.getUserNo()).collect(Collectors.toList());
            List<String> dbPhones = users.stream().map(s -> s.getPhone()).collect(Collectors.toList());

            List<String> userNoCollect = userNos.stream().filter(s -> dbUserNos.contains(s)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(userNoCollect)) {
                return R.error("学号" + StringUtils.join(userNoCollect, ",") + "已存在");
            }

            List<String> phoneCollect = phones.stream().filter(s -> dbPhones.contains(s)).collect(Collectors.toList());
            if (CollectionUtil.isNotEmpty(phoneCollect)) {
                return R.error(StringUtils.join("手机号码" + phoneCollect, ",") + "已存在");
            }


            collect.stream().forEach(s -> {
                String education = s.getEducation();
                if (StringUtils.isNotBlank(education)) {
                    s.setEducation(s.getEducation().split("_")[0]);
                }
                String userType = s.getUserType();
                if (StringUtils.isNotBlank(userType)) {
                    s.setUserType(s.getUserType().split("_")[0]);
                }

                s.setPassword(DigestUtil.md5Hex("888888"));
                s.setCreateTime(LocalDateTime.now());
                s.setCreateUser(getUser().getSysUserDTO().getId());
            });

            sysUserService.saveBatch(collect);
            return R.ok("文件导入成功！数据行数：" + collect.size());
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

    @ApiOperation(value = "获取用户数据范围", notes = "获取用户数据范围")
    @GetMapping("getUserDataScope")
    @Log(value = "获取用户数据范围")
    public R<List<SysOrg>> getUserDataScope() {
        Set<String> dataScope = getDataScope();
        QueryWrapper<SysOrg> sysOrgQueryWrapper = new QueryWrapper<>();
        if (CollectionUtil.isNotEmpty(dataScope)) {
            sysOrgQueryWrapper.lambda().in(SysOrg::getId, dataScope);
        }
        List<SysOrg> sysOrgs = sysOrgService.list(sysOrgQueryWrapper);
        return R.ok(sysOrgs);
    }


    @ApiOperation(value = "获取当前用户下的组织人员", notes = "获取当前用户下的组织人员")
    @GetMapping("findOrgUser")
    @Log(value = "获取当前用户下的组织人员")
    public R<List<SysUser>> findOrgUser() {
        List<OrgDTO> sysOrgs = getUser().getSysUserDTO().getSysOrgs();
        List<SysUser> orgUse = Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(sysOrgs)) {
            Set<String> orgIds = sysOrgs.stream().map(s -> s.getId()).collect(Collectors.toSet());
            orgUse = sysUserService.findOrgUser(orgIds);

        }
        return R.ok(orgUse);
    }


}
