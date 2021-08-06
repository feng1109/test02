package com.eseasky.modules.space.vo.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ConfIdVO {

    /** 对用户开放的confId，只判断开放对象，不判断开放时间 */
    private List<String> openList = new ArrayList<String>();

    /** 根据时间段判断对用户开放的confId，判断开放对象和开放时间 */
    private List<String> freeList = new ArrayList<String>();

}
