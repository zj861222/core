package com.framework.core.search.index.callback;


import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.client.Client;

/**
 * 索引器操作的回调
 *
 * @param <T>
 */
public interface ClientCallback<T extends ActionResponse> {
    /**
     * 通过客户端执行Action
     * @param client
     * @return
     */
    ActionFuture<T> execute(final Client client);
}
