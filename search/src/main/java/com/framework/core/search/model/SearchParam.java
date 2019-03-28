package com.framework.core.search.model;

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class SearchParam implements Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	/* 排序字段 ,有先后顺序 */
	private LinkedHashMap<String, SortOrder> sortFields = new LinkedHashMap<String, SortOrder>();
	/* 查询框参数 */
	private QueryBuilder query;
	/* 过滤参数 */
	private QueryBuilder fiter;
//	/* 开始位置 */
//	private int offset = 0;
	/* 页码 */
	private int page = 1;
	/* 要返回条数 */
	private int size = 1;
	/* 返回字段 */
	private List<String> resultFields = new ArrayList<String>();
	/* 是否高亮显示 */
	private boolean highlight = false;
	/* 是否高亮显示 */
	private String highlightPreTag = "<span style=\"color: #CC0000;\">";
	/* 是否高亮显示 */
	private String highlightPostTag = "</span>";

	//如果不需要返回列表的时候可以将SearchType设为count，减少返回
    private SearchType searchType = SearchType.DEFAULT;

	/* 高亮字段 */
	private List<String> highlightFields = new ArrayList<String>();

	/**
	 * 设置统计
	 */
	private List<AbstractAggregationBuilder> aggregationBuilders;

	public LinkedHashMap<String, SortOrder> getSortFields() {
		return sortFields;
	}

	public void setSortFields(LinkedHashMap<String, SortOrder> sortFields) {
		this.sortFields = sortFields;
	}

	public QueryBuilder getQuery() {
		return query;
	}

	public void setQuery(QueryBuilder query) {
		this.query = query;
	}

	public QueryBuilder getFiter() {
		return fiter;
	}

	public void setFiter(QueryBuilder fiter) {
		this.fiter = fiter;
	}

//	public int getOffset() {
//		return offset;
//	}
//
//	public void setOffset(int offset) {
//		this.offset = offset;
//	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getSize() {
		return size == 0 ? 60 : size;
	}

	public void setSize(int size) {
		this.size = size == 0 ? 60 : size;
	}

	public List<String> getResultFields() {
		return resultFields;
	}

	public void setResultFields(List<String> resultFields) {
		this.resultFields = resultFields;
	}

	public boolean isHighlight() {
		return highlight;
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}

	public String getHighlightPreTag() {
		return highlightPreTag;
	}

	public void setHighlightPreTag(String highlightPreTag) {
		this.highlightPreTag = highlightPreTag;
	}

	public String getHighlightPostTag() {
		return highlightPostTag;
	}

	public void setHighlightPostTag(String highlightPostTag) {
		this.highlightPostTag = highlightPostTag;
	}

	public List<String> getHighlightFields() {
		return highlightFields;
	}

	public void setHighlightFields(List<String> highlightFields) {
		this.highlightFields = highlightFields;
	}

	public List<AbstractAggregationBuilder> getAggregationBuilders() {
		return aggregationBuilders;
	}

	public void setAggregationBuilders(List<AbstractAggregationBuilder> aggregationBuilders) {
		this.aggregationBuilders = aggregationBuilders;
	}

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    @Override
	public SearchParam clone() {
		SearchParam param = null;
		try {
			param = (SearchParam) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return param;
	}
}
