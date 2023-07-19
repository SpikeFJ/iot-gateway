package com.jfeng.gateway.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Utils {
    public static String getAddressInfo(SocketAddress socketAddress) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return getIp(inetSocketAddress) + ":" + getPort(inetSocketAddress);
    }

    public static String getIp(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getHostString();
    }

    public static int getPort(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getPort();
    }

    /**
     * 显示距今间隔
     *
     * @param second
     * @return
     */
    public static String outSecond(long second) {
        if (second < 60) {
            return second + " 秒";
        } else if (second < 3600) {
            return (second / 60) + " 分钟 " + (second % 60) + " 秒";
        } else if (second < 86400) {
            return (second / 3660) + " 小时 " + ((second / 60) % 60) + " 分钟";
        } else {
            return (second / 86400) + " 天 " + ((second / 3600) % 24) + " 小时";
        }
    }

    /**
     * 显示字节大小描述
     *
     * @param bytes
     * @return
     */
    public static String outBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " Byte";
        } else if (bytes < 1048576) {
            return (bytes / 1024) + " KB";
        } else if (bytes < 1073741824) {
            return (bytes / 1048576) + " MB";
        } else if (bytes < 1099511627776L) {
            return (bytes / 1073741824) + " GB";
        } else {
            return (bytes / 1099511627776L) + " TB";
        }
    }
}
