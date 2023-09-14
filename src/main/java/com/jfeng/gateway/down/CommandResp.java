package com.jfeng.gateway.down;

import com.jfeng.gateway.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 下行命令响应
 *
 * @param <T>
 */
@Data
@Slf4j
public class CommandResp<T> {
    String id;//设备
    String key;//标识唯一请求

    int code;
    String message;
    T data;

    public static CommandResp success(CommandReq req) {
        return success(req, null);
    }

    public static <T> CommandResp<T> success(CommandReq req, T data) {
        CommandResp resp = resp(CommandRespCode.SUCCESS, req);
        if (req.getData() != null) {
            resp.data = data;
        }
        return resp;
    }

    public static CommandResp successSend(CommandReq req) {
        return resp(CommandRespCode.SUCCESS_SEND, req);
    }

    public static CommandResp offline(CommandReq req) {
        return resp(CommandRespCode.OFFLINE, req);
    }

    public static CommandResp timeout(CommandReq req) {
        return resp(CommandRespCode.TIMEOUT, req);
    }

    public static CommandResp fail(CommandReq req, String message) {
        return resp(CommandRespCode.FAIL, req, message);
    }

    private static CommandResp resp(CommandRespCode code, CommandReq req) {
        return resp(code, req, null);
    }

    private static CommandResp resp(CommandRespCode code, CommandReq req, String msg) {
        CommandResp resp = new CommandResp();
        resp.key = req.sendNo;
        resp.id = req.deviceId;
        resp.code = code.getValue();
        if (StringUtils.isNotEmpty(msg)) {
            resp.message = msg;
        }
        return resp;
    }
}
