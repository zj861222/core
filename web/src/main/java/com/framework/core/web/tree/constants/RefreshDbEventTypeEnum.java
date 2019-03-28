package com.framework.core.web.tree.constants;

/**
 * 数据库刷新事件类型
 * @author zhangjun
 *
 */
public enum RefreshDbEventTypeEnum
{

	PLAT_ORG_TREE_CACHE("plat_org_tree_cache"),  /**sys_dep组织机构树形节点刷新**/
	BAS_CM_DISTRICT_TREE_CACHE("bas_cm_district_tree_cache"), /**客户销售区域缓存刷新**/
	BAS_CM_TYPE_TREE_CACHE("bas_cm_type_tree_cache"), /**客户类型缓存刷新**/
	
	;

	private String eventType;

	public String getEventType()
	{
		return eventType;
	}

	public void setEventType(String eventType)
	{
		this.eventType = eventType;
	}

	private RefreshDbEventTypeEnum(String type)
	{

		this.eventType = type;
	}

}