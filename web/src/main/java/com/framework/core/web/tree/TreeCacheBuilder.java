package com.framework.core.web.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.Assert;

import com.framework.core.web.tree.model.CodedTreeData;
import com.framework.core.web.tree.model.TreeData;
import com.google.common.collect.Lists;

/**
 * 
 * 树状结构编码:因为可能会有多个 pid为-1的情况，所以默认增加一个第0层  ，id为-1，pid为 -2
 * 
 * 
 * 
 * @author zhangjun
 *
 */
public class TreeCacheBuilder
{

	/**
	 * 
	 * @param dataList
	 * @param totalFloor 总共多少层，默认会增加一个 0 层
	 */
	public List<CodedTreeData> codedSourceTreeData(List<TreeData> dataList, int maxLv)
	{

		if (CollectionUtils.isEmpty(dataList) || maxLv<=0)
		{
			return null;
		}
		
		TreeData defaultTreeData = new TreeData();
		defaultTreeData.setId(-1L);
		defaultTreeData.setParentId(-999L);
		
		dataList.add(defaultTreeData);
		

		Map<String, List<TreeData>> parentMap = new HashMap<>();

		// 放到以parentid为key的map中
		for (TreeData data : dataList)
		{
			List<TreeData> list = parentMap.get(data.getParentId().toString());
			if (list == null)
			{
				list = Lists.newArrayList(data);
				
				parentMap.put(data.getParentId().toString(), list);
			}
			else
			{
				list.add(data);
			}
		}

		List<CodedTreeData> result = fatchAllCodedData(parentMap, maxLv);

		return result;
	}

	/**
	 * 对所有节点数据进行编码
	 * @param parentMap
	 * @param maxLv
	 * @return
	 */
	private List<CodedTreeData> fatchAllCodedData(Map<String, List<TreeData>> parentMap, int maxLv)
	{

		CodedTreeData rootNode = fetchLv0CodeData(parentMap, maxLv);
		
//		CodedTreeData rootNode = findRootNode(parentMap, maxLv);

		Assert.notNull(rootNode);
		

		// 编码之后的所有的节点信息
		List<CodedTreeData> list = codedTreeView(rootNode, parentMap);

		return list;

	}

	
	/**
	 * 构建lv 0的默认真正根节点，
	 * 
	 * @param parentMap
	 * @param maxLv
	 * @return
	 */
	private CodedTreeData fetchLv0CodeData(Map<String, List<TreeData>> parentMap, int maxLv) {
		
		// 查找根节点
		List<TreeData> rootNodeList = parentMap.get(TreeData.ROOT_REAL_PARENT_Id.toString());

		Assert.isTrue(CollectionUtils.isNotEmpty(rootNodeList) && rootNodeList.size() == 1);
		
		CodedTreeData rootNode = new CodedTreeData();

		rootNode.setId(rootNodeList.get(0).getId());
		rootNode.setParentId(rootNodeList.get(0).getParentId());

		rootNode.setLv(0);
		rootNode.setMaxLv(maxLv);

		long rootNodeCode = 10L;
		rootNode.setNodeCode(rootNodeCode);

		String globalCode = rootNodeCode + "";
		// 补0
		if (maxLv > 1)
		{
			globalCode = globalCode + trailingZero(maxLv);
		}

		rootNode.setGlobalNodeCode(Long.parseLong(globalCode));

		return rootNode;
	}
	
	
	
	
	
//	/**
//	 * 查找根节点
//	 * @param parentMap
//	 * @return
//	 */
//	private CodedTreeData findRootNode(Map<String, List<TreeData>> parentMap, int maxLv)
//	{
//
//		// 查找根节点
//		List<TreeData> rootNodeList = parentMap.get(TreeData.ROOT_PARENT_Id.toString());
//
//		Assert.isTrue(CollectionUtils.isNotEmpty(rootNodeList) && rootNodeList.size() == 1);
//
//		CodedTreeData rootNode = new CodedTreeData();
//
//		rootNode.setId(rootNodeList.get(0).getId());
//		rootNode.setParentId(rootNodeList.get(0).getParentId());
//
//		rootNode.setLv(1);
//		rootNode.setMaxLv(maxLv);
//
//		long rootNodeCode = 10L;
//		rootNode.setNodeCode(rootNodeCode);
//
//		String globalCode = rootNodeCode + "";
//		// 补0
//		if (maxLv > 1)
//		{
//			globalCode = globalCode + trailingZero(maxLv - 1);
//		}
//
//		rootNode.setGlobalNodeCode(Long.parseLong(globalCode));
//
//		return rootNode;
//
//	}

	/**
	 * 从根节点查找所有的
	 * @param rootNode
	 * @param parentMap
	 * @return
	 */
	private List<CodedTreeData> codedTreeView(CodedTreeData rootNode, Map<String, List<TreeData>> parentMap)
	{

		List<CodedTreeData> result = Lists.newArrayList(rootNode);

		
		List<CodedTreeData> fatherList = Lists.newArrayList(rootNode);

		int lvIndex = 0;
		while (true)
		{

			lvIndex++;
			// 查找下一层的所有的
			List<CodedTreeData> nextLvList = buildLvSubNode(fatherList, parentMap);

			if (CollectionUtils.isEmpty(nextLvList))
			{
				break;
			}

			result.addAll(nextLvList);

			fatherList.clear();
			fatherList = nextLvList;
		}

		return result;

	}

	
	/**
	 * 构建一层的所有的节点的子节点编码
	 * @param fatherList
	 * @param parentMap
	 * @return
	 */
	private List<CodedTreeData> buildLvSubNode(List<CodedTreeData> fatherList, Map<String, List<TreeData>> parentMap)
	{

		if (CollectionUtils.isEmpty(fatherList))
		{
			return null;
		}

		List<CodedTreeData> result = new ArrayList<>();
		
		for (CodedTreeData fatherNode : fatherList)
		{
			List<CodedTreeData> temList = buildSubNode(fatherNode, parentMap);

			if (CollectionUtils.isNotEmpty(temList))
			{
				result.addAll(temList);
			}
		}

		return result;

	}


	/**
	 * 构建单个节点的子节点编码
	 * @param fatherNode
	 * @param parentMap
	 * @return
	 */
	private List<CodedTreeData> buildSubNode(CodedTreeData fatherNode, Map<String, List<TreeData>> parentMap)
	{

		List<TreeData> sublist = parentMap.get(fatherNode.getId() + "");

		if (CollectionUtils.isEmpty(sublist))
		{
			return null;
		}

		List<CodedTreeData> result = new ArrayList<>();

		int index = 1;

		for (TreeData data : sublist)
		{

			CodedTreeData codedNode = new CodedTreeData();

			codedNode.setId(data.getId());
			codedNode.setParentId(fatherNode.getId());

			codedNode.setLv(fatherNode.getLv() + 1);
			codedNode.setMaxLv(fatherNode.getMaxLv());

			StringBuilder nodeCodeSb = new StringBuilder();
			nodeCodeSb.append(fatherNode.getNodeCode()); // 10
			nodeCodeSb.append(index >= 10 ? index : "0" + index);// 1001
			codedNode.setNodeCode(Long.parseLong(nodeCodeSb.toString()));

			if (codedNode.getLv() < codedNode.getMaxLv())
			{
				nodeCodeSb.append(trailingZero(codedNode.getMaxLv() - codedNode.getLv()));
			}
			codedNode.setGlobalNodeCode(Long.parseLong(nodeCodeSb.toString()));

			result.add(codedNode);

			index++;
		}

		return result;
	}

	private String trailingZero(int lvDistance)
	{

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lvDistance; i++)
		{
			sb.append("00"); // 差1个lv，补2个0
		}

		return sb.toString();
	}

}