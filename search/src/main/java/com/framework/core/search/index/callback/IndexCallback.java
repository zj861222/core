package com.framework.core.search.index.callback;

import com.framework.core.search.index.factory.IndexClient;

/**
 */
public interface IndexCallback{

    public void execute(IndexClient indexClient) throws Exception;

}
