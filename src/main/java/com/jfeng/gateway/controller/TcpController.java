package com.jfeng.gateway.controller;

import com.jfeng.gateway.server.TcpServer;
import com.jfeng.gateway.session.TcpSession;
import com.jfeng.gateway.util.DateTimeUtils2;
import com.jfeng.gateway.util.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@CrossOrigin("*")
public class TcpController {

    @Resource
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
        serverInfos.put("localAddress", tcpServer.getLocalAddress());
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
        int skip = req.skip(onLines.size());
        List<Map<String, Object>> collect = onLines.values().parallelStream().filter(x -> {
            if (StringUtils.isNotEmpty(req.getDeviceId())) {
                return req.getDeviceId().equals(x.getDeviceId());
            }
            if (StringUtils.isNotEmpty(req.getBusinessId())) {
                return req.getDeviceId().equals(x.getBId());
            }
            return true;
        }).skip(skip).limit(req.getPageSize()).map(x -> x.toOnlineJson()).collect(Collectors.toList());
        return Resp.success(collect);
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

        Map<String, TcpSession> onLines = tcpServer.getConnected();
        int skip = req.skip(onLines.size());
        List<Map<String, Object>> collect = onLines.values().parallelStream().filter(x -> {
            if (StringUtils.isNotEmpty(req.getDeviceId())) {
                return req.getDeviceId().equals(x.getDeviceId());
            }
            if (StringUtils.isNotEmpty(req.getBusinessId())) {
                return req.getDeviceId().equals(x.getBId());
            }
            return true;
        }).skip(skip).limit(req.getPageSize()).map(x -> x.toConnectJson()).collect(Collectors.toList());

        return Resp.success(collect);
    }
}
