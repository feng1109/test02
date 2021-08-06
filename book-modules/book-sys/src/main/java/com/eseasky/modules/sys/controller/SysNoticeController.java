package com.eseasky.modules.sys.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.common.code.sys.vo.NoticeAddVO;
import com.eseasky.common.code.sys.vo.NoticeEditVO;
import com.eseasky.common.code.sys.vo.NoticePageVO;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.entity.SysNotice;
import com.eseasky.common.service.SysNoticeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "通知管理", tags = "通知管理")
@RestController
@RequestMapping("/others/noticeManage")
public class SysNoticeController {

	@Autowired
	SysNoticeService noticeService;

	/**
	 * 通过id查询通知
	 */
	@ApiOperation(value = "查询通知", notes = "查询通知")
	@GetMapping("{id}")
	@PreAuthorize("hasAuthority('others:noticeManage:get')")
	public R<SysNotice> query(@PathVariable("id") String id) {
		return noticeService.query(id);
	}

	/**
	 * 通知分页查询
	 */
	@ApiOperation(value = "通知分页查询", notes = "通知分页查询")
	@GetMapping()
	@PreAuthorize("hasAuthority('others:noticeManage:page')")
	public R<Page<SysNotice>> page(@Validated NoticePageVO noticeVO) {
		return noticeService.page(noticeVO);
	}

	/**
	 * 新增通知
	 */
	@ApiOperation(value = "新增通知", notes = "新增通知")
	@PostMapping()
	@PreAuthorize("hasAuthority('others:noticeManage:add')")
	public R<Object> add(@RequestBody @Validated NoticeAddVO noticeVO) {
		return noticeService.add(noticeVO);
	}

	/**
	 * 编辑通知
	 */
	@ApiOperation(value = "编辑通知", notes = "编辑通知")
	@PutMapping()
	@PreAuthorize("hasAuthority('others:noticeManage:update')")
	public R<Object> edit(@RequestBody @Validated NoticeEditVO sysNotice) {
		return noticeService.edit(sysNotice);
	}

	/**
	 * 通过id删除通知
	 */
	@ApiOperation(value = "删除通知", notes = "删除通知")
	@DeleteMapping("{id}")
	@PreAuthorize("hasAuthority('others:noticeManage:delete')")
	public R<Object> delete(@PathVariable("id") String id) {
		return noticeService.delete(id);
	}
	
	/**
	 * 通过id删除通知
	 */
	@ApiOperation(value = "删除通知", notes = "删除通知")
	@DeleteMapping()
	@PreAuthorize("hasAuthority('others:noticeManage:delete')")
	public R<Object> deletes(@RequestBody String ids) {
		return noticeService.deletes(ids);
	}
}
