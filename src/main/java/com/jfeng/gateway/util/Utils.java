package com.jfeng.gateway.util;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Utils {
    public static String getAddressInfo(SocketAddress socketAddress) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return getIp(inetSocketAddress) + ":" + getPort(inetSocketAddress);
    }

    public static String getIp(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getHostName();
    }

    public static int getPort(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getPort();
    }

}
