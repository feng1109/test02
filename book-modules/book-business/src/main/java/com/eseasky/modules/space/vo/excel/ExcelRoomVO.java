package com.eseasky.modules.space.vo.excel;

import java.io.Serializable;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * <p>
 * 导入房间数据
 * </p>
 *
 * @author
 * @since 2021-04-09
 */
@Data
public class ExcelRoomVO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Excel(name = "所属场馆")
    private String buildName;

    @Excel(name = "所属楼层")
    private String floorName;

    @Excel(name = "空间名称")
    private String roomName;

    @Excel(name = "门牌号")
    private String roomNum;

    @Excel(name = "所属区域")
    private String area;

}
