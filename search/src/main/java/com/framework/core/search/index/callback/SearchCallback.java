package com.framework.core.search.index.callback;

import com.framework.core.search.index.factory.IndexClient;

/**
 */
public interface SearchCallback<T>{

    public T execute(IndexClient indexClient);

}
