package com.framework.core.search.model;

import org.elasticsearch.search.aggregations.Aggregation;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SearchResult implements Serializable {
	
	private static final long serialVersionUID = 1L;

    /* 结果 */
    private List<Map<String, Object>> resultList;
    
    private long total;
    
    private int page;
    
    private long totalPage;
    
    private Map<String, Aggregation> aggMaps;

	public List<Map<String, Object>> getResultList() {
		return resultList;
	}

	public void setResultList(List<Map<String, Object>> resultList) {
		this.resultList = resultList;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Map<String, Aggregation> getAggMaps() {
		return aggMaps;
	}

	public void setAggMaps(Map<String, Aggregation> aggMaps) {
		this.aggMaps = aggMaps;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public long getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(long totalPage) {
		this.totalPage = totalPage;
	}
}
