package com.jfeng.gateway.comm;

public class Constant {
    //<------------在线设备流量信息---------->
    public static final String SYSTEM_PREFIX = "iot:";
    public static final String MODULE_MACHINE = SYSTEM_PREFIX + "machine:";
    public static final String MACHINE_SUMMARY = MODULE_MACHINE + "%s" + ":SUMMARY";
    public static final String MACHINE_CONNECTED = MODULE_MACHINE + "%s" + ":CONNECTED";
    public static final String MACHINE_ONLINE = MODULE_MACHINE + "%s" + ":ONLINE";

    public static final Integer SO_BACKLOG = 128;
    public static final Integer SO_RCVBUF = 1024 * 8;
    public static final Integer SO_SNDBUF = 1024 * 8;

    /**
     * 设备和所在机器映射关系
     */
    public static final String ONLINE_MAPPING = SYSTEM_PREFIX + "online:mapping:";

    //<-----------------在线数据----------------------->
    public static final String ONLINE_INFO_REMOTE_ADDRESS = "remote_address";
    public static final String ONLINE_INFO_PACKET_ID = "packet_id";
    public static final String ONLINE_INFO_ID = "id";
    public static final String ONLINE_INFO_CREATE_TIME = "create_time";
    public static final String ONLINE_INFO_RECEIVE_PACKETS = "receive_packets";
    public static final String ONLINE_INFO_RECEIVE_BYTES = "receive_bytes";
    public static final String ONLINE_INFO_LAST_RECEIVE_TIME = "last_receive_time";
    public static final String ONLINE_INFO_SEND_PACKETS = "send_packets";
    public static final String ONLINE_INFO_SEND_BYTES = "send_bytes";
    public static final String ONLINE_INFO_LAST_SEND_TIME = "last_send_time";
    public static final String ONLINE_INFO_LAST_REFRESH_TIME = "last_refresh_time";
    public static final String ONLINE_INFO_TOTAL_MILL = "total_mill";

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

    //<!--------------------启动参数---------------------------->
    public static final String PORT = "PORT";
    public static final String ENABLE = "ENABLE";
    public static final String BLACK_IP_LIST = "BLACK_IP_LIST";
    public static final String WHITE_IP_LIST = "WHITE_IP_LIST";
    //TCP 检测周期
    public static final String TCP_SO_BACKLOG = "TCP_SO_BACKLOG";
    public static final String TCP_SO_RCV_BUF = "TCP_SO_RCV_BUF";
    public static final String TCP_SO_SND_BUF = "TCP_SO_SND_BUF";
    public static final String TCP_CHECK_PERIOD = "TCP_CHECK_PERIOD";
    //TCP登陆超时
    public static final String TCP_LOGIN_TIMEOUT = "TCP_LOGIN_TIMEOUT";
    //TCP心跳超时
    public static final String TCP_HEART_TIMEOUT = "TCP_HEART_TIMEOUT";
}
