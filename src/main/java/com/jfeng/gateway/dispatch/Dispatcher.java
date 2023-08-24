package com.jfeng.gateway.dispatch;

import com.jfeng.gateway.message.DispatchMessage;

/**
 * 分发接口，默认spring内部通知
 */
public interface Dispatcher {

    void sendNext(String packageId, DispatchMessage data);
}
