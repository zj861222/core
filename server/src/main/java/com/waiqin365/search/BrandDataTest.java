package com.waiqin365.search;

import com.framework.core.search.model.IndexData;

public class BrandDataTest extends IndexData
{

	public BrandDataTest()
	{

	}

	public BrandDataTest(Long id, String brandName, String hotKeyword, String brandKeyword)
	{

		this.id = id;

		this.brandKeyword = brandKeyword;

		this.hotKeyword = hotKeyword;

		this.brandName = brandName;
	}

	@Override
	public String fetchIndexId()
	{

		return id == null ? null : id.toString();
	}

	private Long id;

	private String brandName;

	private String hotKeyword;

	private String brandKeyword;

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public String getBrandName()
	{
		return brandName;
	}

	public void setBrandName(String brandName)
	{
		this.brandName = brandName;
	}

	public String getHotKeyword()
	{
		return hotKeyword;
	}

	public void setHotKeyword(String hotKeyword)
	{
		this.hotKeyword = hotKeyword;
	}

	public String getBrandKeyword()
	{
		return brandKeyword;
	}

	public void setBrandKeyword(String brandKeyword)
	{
		this.brandKeyword = brandKeyword;
	}

}