//package com.framework.core.view;
//
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.util.Assert;
//
//
//public abstract class  IndexData {
//	
//
//	public abstract String fetchIndexId();
//	
//	/**
//	 * 获取index 名字
//	 * 
//	 * 
//	 * @return
//	 */
//	public  String fetchIndexName() {
//		
//		Document document = this.getClass().getAnnotation(Document.class);
//		
//		Assert.notNull(document, "You must set the @document tag for the elasticsearch domain!");
//
//		String indexName = document.indexName();
//		
//		Assert.notNull(indexName, "You must set the indexName value in @document tag!");
//		
//		return indexName;
//	}
//	
//	
//	
//
//	/**
//	 * 获取index type
//	 * @return
//	 */
//	public <T> String fetchIndexType() {
//		
//
//		Document document = this.getClass().getAnnotation(Document.class);
//		
//		Assert.notNull(document, "You must set the @document tag for the elasticsearch domain!");
//
//		String indexType = document.indexStoreType();
//		
//		
//		if(StringUtils.isEmpty(indexType)) {
//			
//			indexType = fetchIndexName()+"_def_type";
//		}
//		
//
//		return indexType;
//	}
//
//	
//}