package com.waiqin365.search;


import javax.annotation.Resource;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.framework.core.common.view.BizResult;
import com.framework.core.search.index.service.IndexService;
import com.framework.core.search.model.SearchParam;
import com.framework.core.search.model.SearchResult;

@Controller
public class SearchApiController{
	
	private String indexName = "brand";
	
	@Resource
	private IndexService indexService;
	
	/**
	 * 
	 * @param index
	 * @return
	 */
	@RequestMapping(value = "indexExists")
	@ResponseBody
	public BizResult indexExists(int index)
	{
		
		boolean isExist = indexService.indexExists(indexName);
		
		return BizResult.success(isExist);
	}
	
	
	@RequestMapping(value = "createIndex")
	@ResponseBody
	public BizResult createIndex()
	{
	
		try
		{
			indexService.createIndex(indexName, true);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return BizResult.success();
		
	}
	
	
	@RequestMapping(value = "addIndexData")
	@ResponseBody
	public BizResult addIndexData()
	{
		BrandDataTest b = new BrandDataTest();
		b.setId(111L);
		b.setHotKeyword("huawei");
		b.setBrandName("huawei-rongyao");
		b.setBrandKeyword("huawei");
		
		try
		{
			indexService.addIndexData(indexName, b);
			
			return BizResult.success();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return BizResult.failed(e, e.getMessage());
		}

	}
	
	
	@RequestMapping(value = "idsQuery")
	@ResponseBody
	public BizResult idsQuery()
	{
		
		String[] ids = new String[]{"111"};
		SearchParam parm = new SearchParam();
		
		BoolQueryBuilder filter = QueryBuilders.boolQuery();
		filter.filter(QueryBuilders.idsQuery().addIds(ids));
		
		parm.setQuery(filter);
		SearchResult result = indexService.search(indexName, parm);

		
		return BizResult.success(result);
		
	}
	
	
	@RequestMapping(value = "rebuildIndex")
	@ResponseBody
	public BizResult rebuildIndex()
	{
		
		
		indexService.rebuild(indexName);

		return BizResult.success();
	}
	
	
}

