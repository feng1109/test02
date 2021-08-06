package com.eseasky.modules.space.config;

import java.util.HashMap;
import java.util.Map;

public interface SpaceConstant {

    String spaceConfCache = "space::conf";
    String spaceGroupCache = "space::group";
    String spaceBuildDistanceCache = "space::build::distance"; // 为了计算距离缓存了所有的场馆

    String MOBILE_NO_SPACE = "暂无适合您的空间！";

    public interface StateName {
        Map<Integer, String> MAP = new HashMap<Integer, String>() {
            private static final long serialVersionUID = 1l;
            {
                put(0, "关闭");
                put(1, "开放");
            }
        };
    }
    public interface OrderTypeName {
        Map<Integer, String> MAP = new HashMap<Integer, String>() {
            private static final long serialVersionUID = 1l;
            {
                put(1, "单人短租");
                put(2, "单人长租");
                put(3, "多人短租");
                put(4, "多人长租");
            }
        };
    }
    public interface OrderType {
        int SINGLE_ONCE = 1;
        int SINGLE_LONG = 2;
        int MULTI_ONCE = 3;
        int MULTI_LONG = 4;
        int SINGLE_GROUP = 5;
    }

    public interface SpaceTypeName {
        Map<Integer, String> MAP = new HashMap<Integer, String>() {
            private static final long serialVersionUID = 1l;
            {
                put(1, "场馆");
                put(2, "楼层");
                put(3, "空间");
                put(4, "座位");
                put(5, "座位组");
            }
        };
    }

    public interface Week {
        Map<Integer, String> MAP = new HashMap<Integer, String>() {
            private static final long serialVersionUID = 1l;
            {
                put(1, "周一");
                put(2, "周二");
                put(3, "周三");
                put(4, "周四");
                put(5, "周五");
                put(6, "周六");
                put(7, "周日");
            }
        };
    }
    public interface FloorMove {
        int UP = 1;
        int DOWN = 2;
    }

    /** 前端画布的类型：1座位;2课桌;3墙;4门;5窗户;6插座;7空调;8卫生间 */
    public interface CanvasType {
        int SEAT = 1; // 座位
        int DESK = 2; // 课桌
        int WALL = 3; // 墙
        int DOOR = 4; // 门
        int WINDOW = 5; // 窗户
        int SOCKET = 6; // 插座
        int AIR = 7; // 空调
        int TOILET = 8; // 卫生间
    }
    /** 前端画布的状态：由画布状态、订单状态、座位本身状态构成 */
    public interface CanvasState {
        int CANVAS_NO = 0; // 0画布不可用状态
        int NONE_ORDER = 1; // 1空闲未预约状态
        int ALREADY_ORDER = 2; // 2预约使用中
        int LEAVE_ORDER = 3; // 3预约暂时中
        int SEAT_CLOSE = 4; // 4座位自身已禁用
    }

    /** 适用于空间的全局常量 */
    public interface Switch {
        int NO = 0; // 否
        int YES = 1; // 是

        int NONE = 0; // 没有
        int HAVE = 1; // 有

        int CLOSE = 0; // 综合楼、楼层、空间、座位等 关闭|禁用
        int OPEN = 1; // 综合楼、楼层、空间、座位等 开放|可用

    }

    public interface Duty {
        String STUDENT = "1";
        String TEACHER = "2";
    }

    public interface SpaceType {
        int NONE = 0;
        int BUILD = 1;
        int FLOOR = 2;
        int ROOM = 3;
        int SEAT = 4;
        int GROUP = 5;
    }

}
