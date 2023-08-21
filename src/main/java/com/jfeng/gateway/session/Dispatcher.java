package com.jfeng.gateway.session;

/**
 * 分发接口，默认spring内部通知
 */
public interface Dispatcher {

    void sendNext(String packageId, DispatchData data);
}
