package com.framework.core.search.index.callback;

import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.client.ClusterAdminClient;

/**
 * 集群操作回调
 *
 * @param <T>
 */
public interface ClusterCallback<T extends ActionResponse> {
    /**
     *
     * @param admin
     * @return
     */
    ActionFuture<T> execute(ClusterAdminClient admin);
}
