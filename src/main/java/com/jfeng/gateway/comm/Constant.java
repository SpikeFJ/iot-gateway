package com.jfeng.gateway.comm;

public class Constant {
    //<------------在线设备流量信息---------->
    public static final String SYSTEM_PREFIX = "iot:";
    public static final String MODULE_MACHINE = SYSTEM_PREFIX + "machine:";
    public static final String ONLINE_MAPPING = SYSTEM_PREFIX + "online:";
    public static final String SESSION_CURRENT = SYSTEM_PREFIX + "current:";
    public static final String SESSION_HISTORY = SYSTEM_PREFIX + "history:";

    //<-----------------单个会话信息----------------------->
    public static final String SESSION_REMOTE_ADDRESS = "ra";
    public static final String SESSION_LOCAL_ADDRESS = "la";
    public static final String SESSION_DEVICE_ID = "did";
    public static final String SESSION_BID = "bid";
    public static final String SESSION_CREATE_TIME = "create_time";
    public static final String SESSION_RECEIVE_PACKETS = "receive_packets";
    public static final String SESSION_RECEIVE_BYTES = "receive_bytes";
    public static final String SESSION_LAST_RECEIVE_TIME = "last_receive_time";
    public static final String SESSION_SEND_PACKETS = "send_packets";
    public static final String SESSION_SEND_BYTES = "send_bytes";
    public static final String SESSION_LAST_SEND_TIME = "last_send_time";
    public static final String SESSION_LAST_REFRESH_TIME = "last_refresh_time";
    public static final String SESSION_TOTAL_MILL = "total_mill";

    //<------------服务器流量信息---------->
    public static final String MACHINE = "machine";
    public static final String ONLINE = "online";
    public static final String CONNECTED = "connected";
    public static final String SERVER_CONNECT_NUM = "total_connect_num";
    public static final String SERVER_CLOSE_NUM = "total_close_num";
    public static final String SERVER_SEND_PACKETS = "total_send_packets";
    public static final String SERVER_SEND_BYTES = "total_send_bytes";
    public static final String SERVER_RECEIVE_PACKETS = "total_receive_packets";
    public static final String SERVER_RECEIVE_BYTES = "total_receive_bytes";
    public static final String SERVER_WAIT_TO_SEND = "total_wait_to_send";
    public static final String SERVER_SENDING = "total_sending";
    public static final String SERVER_LAST_REFRESH_TIME = "last_refresh_time";

    //<----日志附加信息------>
    public static final String LOG_TRANSACTION_ID = "TRANSACTION_ID";
    public static final String LOG_ADDRESS = "ADDRESS";
}
