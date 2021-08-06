package com.eseasky.modules.sys.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.eseasky.common.code.sys.vo.NoticePageVO;
import com.eseasky.common.code.utils.R;
import com.eseasky.common.entity.SysNotice;
import com.eseasky.common.service.SysNoticeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "小程序通知查询", tags = "小程序通知查询")
@RestController
@RequestMapping("/notice")
public class AppNoticeController {

	@Autowired
	SysNoticeService noticeService;

	/**
	 * 通过id查询通知
	 */
	@ApiOperation(value = "查询通知", notes = "查询通知")
	@GetMapping("{id}")
	public R<SysNotice> query(@PathVariable("id") String id) {
		return noticeService.query2(id);
	}

	/**
	 * 通知分页查询
	 */
	@ApiOperation(value = "通知分页查询", notes = "通知分页查询")
	@GetMapping()
	public R<Page<SysNotice>> page(@Validated NoticePageVO noticeVO) {
		return noticeService.page2(noticeVO);
	}
}
