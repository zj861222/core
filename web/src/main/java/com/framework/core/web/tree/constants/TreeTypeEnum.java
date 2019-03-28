package com.framework.core.web.tree.constants;


/**
 * 树形节点枚举类型
 * 
 * @author zhangjun
 *
 */
public enum TreeTypeEnum {
	
	//sysDepartment 缓存
	ORG_TREE_CACHE("org_tree_cache",7,RefreshDbEventTypeEnum.PLAT_ORG_TREE_CACHE),

	//bas_cm_customer_type 缓存
	BAS_CM_TYPE_TREE_CACHE("bas_cmr_type_tree_cache", 10, RefreshDbEventTypeEnum.BAS_CM_TYPE_TREE_CACHE),

	//bas_cm_district 缓存
	BAS_CM_DISTRICT_TREE_CACHE("bas_cm_district_tree_cache", 10, RefreshDbEventTypeEnum.BAS_CM_DISTRICT_TREE_CACHE), ;
	
	;
	
	
	private String treeType;
	
	
	private int maxLv;
	

	private RefreshDbEventTypeEnum refreshDbType;
	

	
	public RefreshDbEventTypeEnum getRefreshDbType()
	{
		return refreshDbType;
	}

	public void setRefreshDbType(RefreshDbEventTypeEnum refreshDbType)
	{
		this.refreshDbType = refreshDbType;
	}

	public int getMaxLv()
	{
		return maxLv;
	}

	public void setMaxLv(int maxLv)
	{
		this.maxLv = maxLv;
	}

	public String getTreeType()
	{
		return treeType;
	}

	public void setTreeType(String treeType)
	{
		this.treeType = treeType;
	}
	
	
	
	public static TreeTypeEnum getTreeTypeEnum(String type) {
		
		TreeTypeEnum[] all = TreeTypeEnum.values();
		
		if(all == null || all.length ==0) {
			return null;
		}
		
		
		for(TreeTypeEnum typeEnum:all) {
			
			if(typeEnum.getTreeType().equals(type)){
				return typeEnum;
			}
			
		}
		
		return null;
		
	}



	private TreeTypeEnum(String type,int maxLv ,RefreshDbEventTypeEnum refreshDbType){
		
		this.treeType = type;
		
		this.maxLv = maxLv;
		
		this.refreshDbType = refreshDbType;
	}
	
}