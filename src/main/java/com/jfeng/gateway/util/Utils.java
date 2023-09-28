package com.jfeng.gateway.util;

import java.net.*;
import java.util.Enumeration;
import java.util.Map;

public class Utils {
    public static String getAddressInfo(SocketAddress socketAddress) {
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return getIp(inetSocketAddress) + ":" + getPort(inetSocketAddress);
    }

    /**
     * 会获取到127.0.0.1
     * @return
     */
    public static String getLocalIp() {
        try {
            final InetAddress localHost = InetAddress.getLocalHost();
            final String hostAddress = localHost.getHostAddress();
            return hostAddress;
        } catch (Exception e) {
            return "UNKNOW";
        }
    }

    public static String getIpAddress() {
        try {
            //从网卡中获取IP
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress ip;
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces.nextElement();
                //用于排除回送接口,非虚拟网卡,未在使用中的网络接口
                if (!netInterface.isLoopback() && !netInterface.isVirtual() && netInterface.isUp()) {
                    //返回和网络接口绑定的所有IP地址
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip instanceof Inet4Address) {
                            return ip.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("IP地址获取失败" + e.toString());
        }
        return "";
    }

    public static String getIp(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getHostString();
    }

    public static int getPort(InetSocketAddress inetSocketAddress) {
        return inetSocketAddress.getPort();
    }


    public static String get(Map<String, String> source, String key, String defaultValue) {
        if (source.containsKey(key)) return source.get(key);
        return defaultValue;
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
            return (second / 60) + " 分 " + (second % 60) + " 秒";
        } else if (second < 86400) {
            return (second / 3660) + " 时 " + ((second / 60) % 60) + " 分";
        } else {
            return (second / 86400) + " 天 " + ((second / 3600) % 24) + " 时";
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
//
//    public static class Value {
//        public String value0;//体积含水量
//        public String value1;//温度
//        public String value2;//电导率
//        public String value3;//PH值
//        public String value4;//氮含量
//        public String value5;//磷含量
//        public String value6;//钾含量
//        public String value7;//盐分
//
//        public void read03(byte[] bytes) {
//            int offset = 0;
//            int deviceAddress = bytes[offset++];
//            int function = bytes[offset++];
//            int num = bytes[offset++];
//
//            int tmp0 = unit16(bytes, offset);
//            value0 = new BigDecimal(String.valueOf(tmp0)).divide(new BigDecimal(10d), BigDecimal.ROUND_HALF_UP).toPlainString();
//            offset += 2;
//
//            int tmp1 = sign(bytes, offset);
//            value1 = new BigDecimal(String.valueOf(tmp1)).divide(new BigDecimal(10.0d)).toPlainString();
//            offset += 2;
//
//            value2 = String.valueOf(unit16(bytes, offset));
//            offset += 2;
//
//            int tmp3 = unit16(bytes, offset);
//            value3 = new BigDecimal(String.valueOf(tmp3)).divide(new BigDecimal(10d), BigDecimal.ROUND_HALF_UP).toPlainString();
//            offset += 2;
//
//            value4 = String.valueOf(unit16(bytes, offset));
//            offset += 2;
//
//            value5 = String.valueOf(unit16(bytes, offset));
//            offset += 2;
//
//            value6 = String.valueOf(unit16(bytes, offset));
//            offset += 2;
//
//            value7 = String.valueOf(unit16(bytes, offset));
//            offset += 2;
//        }
//
//        private int unit16(byte[] source, int offset) {
//            return ((source[offset] & 0xFF) << 8) + (source[offset + 1] & 0xFF);
//        }
//
//        private int sign(byte[] source, int offset) {
//            return ((source[offset]) << 8) + (source[offset + 1]);
//        }
//
//        public void print() {
//            System.out.printf("体积含水量: %s RH\n", value0);
//            System.out.printf("温度: %s ℃\n", value1);
//            System.out.printf("电导率: %s us/cm\n", value2);
//            System.out.printf("ph: %s\n", value3);
//            System.out.printf("氮含量: %s mg/kg\n", value4);
//            System.out.printf("磷含量: %s mg/kg\n", value5);
//            System.out.printf("钾含量: %s mg/kg\n", value6);
//            System.out.printf("盐分: %s mg/kg\n", value7);
//        }
//    }
//
//    public static void main(String[] args) {
//        Value value = new Value();
//        value.read03(ByteBufUtil.decodeHexDump("01031003E8ffe7029D00510021002E006B001990AC"));
//        value.print();
//    }
}
