package com.jfeng.gateway.controller;

import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.StringUtils;
import com.jfeng.gateway.util.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@CrossOrigin("*")
public class TcpController {

    @Autowired
    TcpServer tcpServer;

    /**
     * 首页统计数据
     *
     * @return
     */
    @RequestMapping(path = "/summary", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Resp summary() {
        Map<String, Object> serverInfos = new HashMap<>();
        serverInfos.put("online", tcpServer.getOnLines().size());
        serverInfos.put("connect", tcpServer.getConnected().size());
        serverInfos.put("send", tcpServer.getSynSent().size());
        serverInfos.put("transfer", "0");

        serverInfos.put("createTime", DateTimeUtils2.outString(tcpServer.getCreateTime()));
        serverInfos.put("createTimeDuration", Utils.outSecond(Duration.between(tcpServer.getCreateTime(), LocalDateTime.now()).getSeconds()));
        serverInfos.put("localAddress", tcpServer.getLocalAddress());
        serverInfos.put("localPort", tcpServer.getPort());
        serverInfos.put("totalConnectNum", tcpServer.getTotalConnectNum());
        serverInfos.put("totalCloseNum", tcpServer.getTotalCloseNum());
        serverInfos.put("totalSendPackets", tcpServer.getTotalSendPackets());
        serverInfos.put("totalSendBytes", tcpServer.getTotalSendBytes());
        serverInfos.put("totalReceivePackets", tcpServer.getTotalReceivePackets());
        serverInfos.put("totalReceiveBytes", tcpServer.getTotalReceiveBytes());

        return Resp.success(serverInfos);
    }

    /**
     * 当前在线终端
     *
     * @param req
     * @return
     */
    @RequestMapping(path = "/tcp/onlineList", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Resp onlineList(@RequestBody(required = false) Req req) {
        if (req == null) {
            List<Map<String, Object>> collect = tcpServer.getOnLines().values().parallelStream().map(x -> x.toOnlineJson()).collect(Collectors.toList());
            return Resp.success(collect);
        }

        Map<String, TcpSession> onLines = tcpServer.getOnLines();
        int skip = PageInfo.skip(onLines.size(), req.getPageNum(), req.getPageSize());

        List<Map<String, Object>> collect = onLines.values().parallelStream().filter(x -> {
            if (StringUtils.isNotEmpty(req.getQuery())) {
                return req.getQuery().equalsIgnoreCase(x.getDeviceId())
                        || req.getQuery().equalsIgnoreCase(x.getBId())
                        || x.getRemoteAddress().contains(req.getQuery());
            }
            return true;
        }).skip(skip).limit(req.getPageSize()).map(x -> x.toOnlineJson()).collect(Collectors.toList());

        return Resp.success(PageInfo.create(onLines.size(), collect, req.getPageSize(), req.getPageNum()));
    }

    /**
     * 当前连接
     *
     * @param req
     * @return
     */
    @RequestMapping(path = "/tcp/connectList", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Resp connectList(@RequestBody(required = false) Req req) {
        if (req == null) {
            List<Map<String, Object>> collect = tcpServer.getConnected().values().parallelStream().map(x -> x.toConnectJson()).collect(Collectors.toList());
            return Resp.success(collect);
        }

        Map<String, TcpSession> connected = tcpServer.getConnected();
        int skip = PageInfo.skip(connected.size(), req.getPageNum(), req.getPageSize());

        List<Map<String, Object>> collect = connected.values().parallelStream().filter(x -> {
            if (StringUtils.isNotEmpty(req.getQuery())) {
                return x.getRemoteAddress().contains(req.getQuery());
            }
            return true;
        }).skip(skip).limit(req.getPageSize()).map(x -> x.toConnectJson()).collect(Collectors.toList());

        return Resp.success(PageInfo.create(connected.size(), collect, req.getPageSize(), req.getPageNum()));
    }

    @RequestMapping(path = "/single", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Resp queryDevice(@RequestBody ReqSingle single) {
        if (StringUtils.isEmpty(single.getId())) {
            return Resp.fail("缺少设备编号.");
        }

        TcpSession tcpSession = tcpServer.getOnLines().values().parallelStream()
                .filter(device -> device.getDeviceId().equalsIgnoreCase(single.getId()))
                .findAny().get();
        if (tcpSession != null) {
            return Resp.success(tcpSession.toSingle());
        }
        return Resp.success();
    }


    @RequestMapping(path = "/singleConnect", method = RequestMethod.POST, consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public Resp queryConnect(@RequestBody ReqSingle single) {
        if (StringUtils.isEmpty(single.getId())) {
            return Resp.fail("缺少连接id.");
        }

        TcpSession tcpSession = tcpServer.getConnected().values().parallelStream()
                .filter(device -> device.getChannelId().equalsIgnoreCase(single.getId()))
                .findAny().get();
        if (tcpSession != null) {
            return Resp.success(tcpSession.toSingle());
        }
        return Resp.success();
    }
}
