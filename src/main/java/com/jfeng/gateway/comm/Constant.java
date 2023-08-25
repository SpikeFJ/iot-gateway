package com.jfeng.gateway.comm;

public class Constant {
    //<------------在线设备流量信息---------->
    public static final String SYSTEM_PREFIX = "iot:";
    public static final String MODULE_MACHINE = SYSTEM_PREFIX + "machine:";

    /**
     * 设备和所在机器映射关系
     */
    public static final String ONLINE_MAPPING = SYSTEM_PREFIX + "online:mapping:";

    //<------------服务器流量信息---------->
    public static final String MACHINE = "machine";
    public static final String TOTAL_ONLINE = "total_online";
    public static final String TOTAL_CONNECTED = "total_connected";
    public static final String TOTAL_CONNECT_NUM = "total_connect_num";
    public static final String TOTAL_CLOSE_NUM = "total_close_num";
    public static final String TOTAL_SEND_PACKETS = "total_send_packets";
    public static final String TOTAL_SEND_BYTES = "total_send_bytes";
    public static final String TOTAL_RECEIVE_PACKETS = "total_receive_packets";
    public static final String TOTAL_RECEIVE_BYTES = "total_receive_bytes";
    public static final String LAST_REFRESH_TIME = "last_refresh_time";

    //<----日志附加信息------>
    public static final String LOG_TRANSACTION_ID = "TRANSACTION_ID";
    public static final String LOG_ADDRESS = "ADDRESS";
}
