package com.framework.core.search.index.callback;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.client.IndicesAdminClient;

/**
 * 节点操作回调
 *
 * @param <T>
 */
public interface NodeCallback<T extends ActionResponse> {
    /**
     * 接口索引操作客户端
     * @param client
     * @return
     */
    ActionFuture<T> execute(final IndicesAdminClient client);
}
