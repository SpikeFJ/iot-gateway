package com.jfeng.gateway.up.dispatch;

import com.jfeng.gateway.message.DispatchMessage;

/**
 * 数据分发接口，默认spring内部通知
 */
public interface DataDispatcher {

    void sendNext(String packageId, DispatchMessage data);
}
