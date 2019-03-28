//package com.framework.core.view;
//
//import org.springframework.data.elasticsearch.annotations.Document;
//
//@Document(indexName = "test_es_order_index2", type = "test_es_order_type")
//public class Order extends IndexData
//{
//
//	private Long id;
//
//	private String userName;
//
//	private String skuName;
//
//	public Long getId()
//	{
//		return id;
//	}
//
//	public void setId(Long id)
//	{
//		this.id = id;
//	}
//
//	public String getUserName()
//	{
//		return userName;
//	}
//
//	public void setUserName(String userName)
//	{
//		this.userName = userName;
//	}
//
//	public String getSkuName()
//	{
//		return skuName;
//	}
//
//	public void setSkuName(String skuName)
//	{
//		this.skuName = skuName;
//
//	}
//
//	@Override
//	public String toString()
//	{
//		return "Order{" + "id=" + id + ", userName='" + userName + '\'' + ", skuName='" + skuName + '\'' + '}';
//	}
//
//	@Override
//	public String fetchIndexId()
//	{
//		return id + "";
//	}
//}