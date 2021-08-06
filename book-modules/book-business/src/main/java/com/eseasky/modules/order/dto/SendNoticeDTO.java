package com.eseasky.modules.order.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * @describe:
 * @title: SendNoticeDTO
 * @Author lc
 * @Date: 2021/5/17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "推送通知")
@Alias("SendNoticeDTO")
public class SendNoticeDTO {

    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "用户id")
    private String userId;

    @ApiModelProperty(value = "通知类型id")
    private Integer noticeType;

    @ApiModelProperty(value = "标题类型id")
    private Integer titleType;

    @ApiModelProperty(value = "内容类型id")
    private Integer contentType;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "推送时间")
    private Date sendTime;
}