//package com.framework.core.web.tree.impl;
//
//
//import java.util.List;
//
//import javax.annotation.Resource;
//
//import org.springframework.stereotype.Component;
//
//import com.framework.core.web.tree.constants.TreeTypeEnum;
//import com.framework.core.web.tree.model.TreeData;
//
//
//
///**
// * sys_department 构建树形缓存
// * 
// * @author zhangjun
// *
// */
//@Component
//public class SysDepartmentTreeCache extends TreeCacheTemplate {
//
//	
//	@Resource
//	private SysDepartmentDao sysDepartmentDao;
//	
//	
//	@Override
//	public List<TreeData> getAllTreeData()
//	{
//		
//		return sysDepartmentDao.queryAllTreeData();
//
//	}
//
//	@Override
//	public TreeTypeEnum getTreeType()
//	{
//		return TreeTypeEnum.ORG_TREE_CACHE;
//	}
//	
//	
//
//}