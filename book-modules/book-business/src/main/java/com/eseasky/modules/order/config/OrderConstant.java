package com.eseasky.modules.order.config;

public interface OrderConstant {

    String COMMENT_PERSON = "comment:person:"; // 综合楼下的每个房间评论总人数
    String COMMENT_LEVEL = "comment:level:"; // 综合楼下的每个房间评论总星级

    String PERSON_ORDER_BUILD = "person:build:"; // 个人在综合楼的预约总次数
    String PERSON_ORDER_ROOM = "person:room:"; // 个人在综合楼的预约总次数

    interface Switch {
        Integer YES = 1;
        Integer NO = 0;
    }

    interface Number {

    }

    interface OrderType {
        Integer SHORT_RENT = 1;
        Integer LONG_RENT = 2;
        Integer SHORT_RENT_ORDER_MEETING = 3; // 会议室一次性预约
        Integer LONG_RENT_ORDER_MEETING = 4; // 会议室长租预约
        Integer GROUP_RENT = 5;
    }

    interface ApproveState {
        Integer WAIT = 1;
        Integer AGREE = 2;
        Integer REJECT = 3;
        Integer NO_EFFECTIVE = 4;

    }

    /**
     * 短租订单状态
     */
    interface listState {
        Integer WAIT_ARRIVE = 1;
        Integer IN_USE = 2;
        Integer AWAY = 3;
        Integer FINISH = 4;
        Integer NO_COME = 5;
        Integer NO_LEAVE = 6;
        Integer NO_BACK = 7;
        Integer WAIT_LEAVE = 8;
        Integer SELF_CANCEL = 9;
        Integer MANAGER_CANCEL = 10;
        Integer BLACK_CANCEL = 11;
        Integer APPROVE_WAIT = 12;
        Integer APPROVE_REJECT = 13;
        Integer APPROVE_NO_EFFECTIVE = 14;
    }

    /**
     * 订单状态枚举
     */
    enum ListStateEnum {
        WAIT_ARRIVE(1, 1, "待签到", "暂无"),
        IN_USE(2, 2, "使用中", "暂无"),
        AWAY(3, 3, "暂离", "暂离"),
        FINISH(4, 4, "已完成", "暂无"),
        NO_COME(5, 5, "未签到", "未签到"),
        NO_LEAVE(6, 5, "未签退", "未签退"),
        NO_BACK(7, 5, "暂离未返回", "暂离未返回"),
        WAIT_LEAVE(8, 6, "待签退", "暂无"),
        SELF_CANCEL(9, 7, "用户取消", "用户取消"),
        MANAGER_CANCEL(10, 7, "管理员取消", "管理员取消"),
        BLACK_CANCEL(11, 7, "进入黑名单取消", "进入黑名单取消"),
        APPROVE_WAIT(12, 8, "待审批", "暂无"),
        APPROVE_REJECT(13, 9, "审批驳回", "暂无"),
        APPROVE_NO_EFFECTIVE(14, 10, "审批失效", "暂无");

        // 后端id
        private final Integer aftId;

        // 前端展示id
        private final Integer befId;

        // 状态值
        private final String value;

        // 备注
        private final String remark;

        ListStateEnum(Integer aftId, Integer befId, String value, String remark) {
            this.aftId = aftId;
            this.befId = befId;
            this.value = value;
            this.remark = remark;
        }

        public Integer getAftId() {
            return aftId;
        }

        public Integer getBefId() {
            return befId;
        }

        public String getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }


    /**
     * 打卡方式
     */
    interface clockType {
        Integer DISTANCE = 1;
        Integer CODE = 2;
    }

    /**
     * 违规方式
     */
    interface violateType {
        Integer NO_ARRIVE = 1;
        Integer NO_LEAVE = 2;
        Integer NO_BACK = 3;
        Integer BE_LATE = 4;
    }

    /**
     * 黑名单规则类型
     */
    interface blackRuleType {
        Integer CONTINUE_NO_ARRIVE = 1;
        Integer ALWAYS_NO_ARRIVE = 2;
        Integer ALWAYS_NO_BACK = 3;
        Integer ALWAYS_NO_LEAVE = 4;
        Integer ALWAYS_BE_LATE = 5;

    }

    /**
     * 排序类型
     */
    interface sortType {
        Integer CREATE_TIME_ASC = 1;
        Integer CREATE_TIME_DESC = 2;

    }

    /**
     * pc端统计类型
     */
    interface statisticsType {
        Integer ORDER = 1;
        Integer VIOLATE = 2;
        Integer FINISH = 3;
        Integer ADVANCE = 4;
        Integer SPACE = 5;
        Integer STUDENT = 6;

    }

    /**
     * 会议预约状态
     */
    enum MeetingStateEnum {

        TO_START(1, 1, "待开始", "暂无"),
        IN_USE(2, 2, "进行中", "暂无"),
        FINISH(4, 3, "已完成", "暂无"),
        SELF_CANCEL(9, 4, "用户取消", "用户取消"),
        ADMIN_CANCEL(10, 4, "管理员取消", "管理员取消"),
        SYSTEM_CANCEL(11, 4, "系统取消", "系统取消"),
        TO_BE_APPROVE(12, 5, "待审核", "待审核"),
        APPROVE_FAILED(13, 6, "审核不通过", "审核不通过"),
        APPROVE_INVALID(14, 7, "审核失效", "审核失效");

        // 后端id
        private final Integer aftId;

        // 前端展示id
        private final Integer befId;

        // 状态值
        private final String value;

        // 备注
        private final String remark;

        MeetingStateEnum(Integer aftId, Integer befId, String value, String remark) {
            this.aftId = aftId;
            this.befId = befId;
            this.value = value;
            this.remark = remark;
        }

        public static boolean isInclude(int key) {
            boolean include = false;
            for (MeetingStateEnum e : MeetingStateEnum.values()) {
                if (e.getAftId() == key) {
                    include = true;
                    break;
                }
            }
            return include;
        }

        public static String getRemark(Integer code) {
            String remark = "";
            for (MeetingStateEnum e : MeetingStateEnum.values()) {
                if (e.getAftId().equals(code)) {
                    remark = e.getRemark();
                }
            }
            return remark;
        }

        public Integer getAftId() {
            return aftId;
        }

        public Integer getBefId() {
            return befId;
        }

        public String getValue() {
            return value;
        }

        public String getRemark() {
            return remark;
        }
    }

    /**
     * pc端统计类型
     */
    interface OpenCloseMeeting {
        Integer OPEN_APPOINTMENT = 1;
        Integer CLOSE_APPOINTMENT = 2;
    }

    /**
     * 会议预约状态
     */
    enum MeetingSignStateEnum {
        NOT_SIGN_TIME(1, "未到签到时间"), TO_BE_SIGN(2, "签到"), SIGNED(3, "已签到"), NOT_SIGN(4, "未签到");

        // 状态值
        private final Integer code;

        // 备注
        private final String remark;

        MeetingSignStateEnum(Integer code, String remark) {
            this.code = code;
            this.remark = remark;
        }

        public static String getRemark(Integer code) {
            String remark = "";
            for (MeetingSignStateEnum e : MeetingSignStateEnum.values()) {
                if (e.getCode().equals(code)) {
                    remark = e.getRemark();
                }
            }
            return remark;
        }

        public Integer getCode() {
            return code;
        }

        public String getRemark() {
            return remark;
        }
    }

    /**
     * 是否需要审批
     */
    enum NeedApprove {
        NOT_APPROVE(0, "不需要审批"), NEED_APPROVE(1, "需要审批");

        // 状态值
        private final Integer code;

        // 备注
        private final String remark;

        NeedApprove(Integer code, String remark) {
            this.code = code;
            this.remark = remark;
        }

        public static String getRemark(Integer code) {
            String remark = "";
            for (NeedApprove e : NeedApprove.values()) {
                if (e.getCode().equals(code)) {
                    remark = e.getRemark();
                }
            }
            return remark;
        }

        public Integer getCode() {
            return code;
        }

        public String getRemark() {
            return remark;
        }
    }

    /**
     * 通知类型
     */
    interface NoticeTemplateId {
        // 打卡提醒
        String CLOCK_REMINDER = "akTvFzEzmmdOU6kTAtIIPfNWFXV8qLge5ffLnTbyzgw";
        // 预约取消通知
        String CANCEL_APPOINTMENT = "vdxlbJyFBHu-KWqfZFwzVv-G7wW1tlGyYznlcOOm4wY";
        // 预约成功通知
        String APPOINTMENT_SUCCESS = "YRwMYeATzqFFlbkrKRFWWdz6qu0R_5KtqD77ab7a6zY";
    }

    /**
     * 通知类型
     */
    interface NoticeType {
        // 微信
        Integer WeChat = 1;
        // 小程序
        Integer App = 2;
    }

    /**
     * 通知类型
     */
    interface NoticeTitleType {
        // 微信
        Integer SEAT = 1;
        // 小程序
        Integer MEETING = 2;
        Integer GROUP = 3;
        Integer SHORT = 4;
        Integer LONG = 5;
        Integer APPROVE = 6;
    }

    /**
     * 展示内容类型
     */
    interface NoticeContentType {
        // 预约审核通知
        Integer APPROVE = 1;
        // 会议室邀请通知
        Integer INVITE = 2;
    }

    public static String wxPage = "/pages/tabbar/punchCard";

    /**
     * 消息队列名
     */
    interface queue {
        String ORDER_LATE_QUEUE = "order_late_queue";
        String ORDER_LEAVE_QUEUE = "order_leave_queue";
        String ORDER_WAIT_LEAVE_QUEUE = "order_wait_leave_queue";
        String BLACK_LIST_QUEUE = "black_list_queue";
        String OUT_BLACK_LIST_QUEUE = "out_black_list_queue";
        String FINISH_LONG_QUEUE = "finish_long_queue";
        String WAIT_LONG_QUEUE = "wait_long_queue";
        String AWAY_BACK_QUEUE = "away_back_queue";
        String GROUP_OFF_QUEUE = "group_off_queue";
        String CANCEL_MEETING_QUEUE = "cancel_meeting_queue";
        String FINISH_MEETING_QUEUE = "finish_meeting_queue";
        String CLOCK_REMINDER_MEETING_QUEUE = "clock_reminder_meeting_queue";
        String GROUP_ARRIVE_QUEUE = "group_arrive_queue";
        String GROUP_WAIT_LEAVE_QUEUE = "group_wait_leave_queue";
        String GROUP_LEAVE_QUEUE = "group_leave_queue";
        String APPROVE_OFF_QUEUE = "approve_off_queue";
        String CLOCK_REMINDER_QUEUE = "clock_reminder_queue";
    }

    /**
     * 消息队列路由
     */
    interface routeKey {
        String ORDER_LATE_ROUTINGKEY = "order_late_routingKey";
        String ORDER_LEAVE_ROUTINGKEY = "order_leave_routingKey";
        String ORDER_WAIT_LEAVE_ROUTINGKEY = "order_wait_leave_routingKey";
        String BLACK_LIST_ROUTINGKEY = "black_list_routingKey";
        String OUT_BLACK_LIST_ROUTINGKEY = "out_black_list_routingKey";
        String FINISH_LONG_ROUTINGKEY = "finish_long_routingKey";
        String WAIT_LONG_ROUTINGKEY = "wait_long_routingKey";
        String AWAY_BACK_ROUTINGKEY = "away_back_routingKey";
        String CANCEL_MEETING_ROUTINGKEY = "cancel_meeting_routingKey";
        String FINISH_MEETING_ROUTINGKEY = "finish_meeting_routingKey";
        String CLOCK_REMINDER_MEETING_ROUTINGKEY = "clock_reminder_meeting_routingKey";
        String GROUP_OFF_ROUTINGKEY = "group_off_routingKey";
        String GROUP_ARRIVE_ROUTINGKEY = "group_arrive_routingKey";
        String GROUP_WAIT_LEAVE_ROUTINGKEY = "group_wait_leave_routingkey";
        String GROUP_LEAVE_ROUTINGKEY = "group_leave_routingKey";
        String APPROVE_OFF_ROUTINGKEY = "approve_off_routingKey";
        String CLOCK_REMINDER_ROUTINGKEY = "clock_reminder_routingKey";
    }


    /**
     * 拼团成员类型
     */
    interface MemberType {
        Integer LAUNCH = 1;
        Integer JOIN = 2;

    }


    /**
     * 拼团订单状态类型
     */
    interface GroupListState {
        Integer GROUPING = 1;
        Integer GROUP_FAIL = 2;
        Integer IN_USE = 3;
        Integer FINISH = 4;
        Integer CANCEL = 5;
    }

    /**
     * 拼团用户状态
     */
    interface GroupUserState {
        Integer GROUPING = 1;
        Integer GROUP_FAIL = 2;
        Integer WAIT_ARRIVE = 3;
        Integer IN_USE = 4;
        Integer WAIT_LEAVE = 5;
        Integer FINISH = 6;
        Integer NO_ARRIVE = 7;
        Integer NO_LEAVE = 8;
        Integer USER_CANCEL = 9;
        Integer USER_QUIT = 10;
        Integer MANAGER_CANCEL = 11;
        Integer BLACK_CANCEL = 12;
    }

    /**
     * 订单状态枚举
     */
    enum GroupUserStateEnum {
        GROUPING(1, "暂无"),
        GROUP_FAIL(2, "暂无"),
        WAIT_ARRIVE(3, "暂无"),
        IN_USE(4, "暂无"),
        WAIT_LEAVE(5, "暂无"),
        FINISH(6, "暂无"),
        NO_ARRIVE(7, "未签到"),
        NO_LEAVE(8, "未签退"),
        USER_CANCEL(9, "用户取消"),
        USER_QUIT(10, "用户退出"),
        MANAGER_CANCEL(11, "管理员取消"),
        BLACK_CANCEL(12, "黑名单取消");


        // 状态值
        private Integer state;

        // 备注
        private String remark;

        GroupUserStateEnum(Integer state, String remark) {
            this.state = state;
            this.remark = remark;
        }

        public Integer getState() {
            return state;
        }

        public String getRemark() {
            return remark;
        }


    }

    /**
     * 反馈处理状态
     */
    interface OrderFeedBackState {
        // 未处理
        Integer NO = 0;
        // 已处理
        Integer YES = 1;
    }

    /**
     * 反馈物品类型
     */
    interface OrderFeedBackType {
        // 桌子
        Integer TABLE = 1;
        // 椅子
        Integer CHAIR = 2;
        // 插座
        Integer SOCKET = 3;
        // 灯光
        Integer LIGHT = 4;
        // 空调
        Integer AIR_CONDITIONING = 5;
        // 其他
        Integer OTHER = 6;
    }

}
