package com.framework.core.web.tree.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.framework.core.web.tree.constants.TreeTypeEnum;


public class TreeNodeUtils
{

	public static final int LV_DISTANCE = 2;

	/**
	 * 根据node code计算 global node code起始指
	 * @param treeTypeEnum
	 * @param nodeCode
	 * @return
	 */
	public static double getGlobalNodeCodeFromScoreForSubNode(TreeTypeEnum treeTypeEnum, long nodeCode)
	{

		// 最大级别
		int maxLv = treeTypeEnum.getMaxLv();

		int startLv = getNodeCodeLv(nodeCode);

		if (startLv == maxLv)
		{
			return nodeCode;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(nodeCode);

		sb.append(trailingZero(maxLv - startLv));

		return Double.parseDouble(sb.toString());

	}

	/**
	 * 根据node code计算 global node code结束指
	 * @param treeTypeEnum
	 * @param nodeCode
	 * @return
	 */
	public static double getGlobalNodeCodeEndScoreForSubNode(TreeTypeEnum treeTypeEnum, long nodeCode)
	{

		// 最大级别
		int maxLv = treeTypeEnum.getMaxLv();

		int startLv = getNodeCodeLv(nodeCode);

		if (startLv == maxLv)
		{
			return nodeCode;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(nodeCode);

		sb.append(trailingNine(maxLv - startLv));

		return Double.parseDouble(sb.toString());
	}

	private static String trailingZero(int lvDistance)
	{

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lvDistance; i++)
		{
			sb.append("00"); // 差1个lv，补2个0
		}

		return sb.toString();
	}

	/**
	 * 用99补充
	 * @param lvDistance
	 * @return
	 */
	private static String trailingNine(int lvDistance)
	{

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < lvDistance; i++)
		{
			sb.append("99"); // 差1个lv，补2个99
		}

		return sb.toString();
	}

	/**
	 * 根据nodecode计算当前lv。 因为有第0层，所以需要额外-1
	 * @param nodeCode
	 * @return
	 */
	public static int getNodeCodeLv(long nodeCode)
	{

		int lv = String.valueOf(nodeCode).length() / LV_DISTANCE;

		return lv-1;

	}

	/**
	 * nodecode的 同一层的起时 节点.
	 * @param nodeCode
	 * @return
	 */
	public static double getNodeCodeFromScoreWithSameLv(long nodeCode)
	{

		int currentLv = getNodeCodeLv(nodeCode);
		//只有1层的，就是自身顶层节点
		if (currentLv == 0)
		{
			return nodeCode;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("10");

		//root是第0层
		sb.append(trailingZero(currentLv));

		return Double.parseDouble(sb.toString());

	}

	/**
	 * nodecode的 同一层的end 节点
	 * @param nodeCode
	 * @return
	 */
	public static double getNodeCodeEndScoreWithSameLv(long nodeCode)
	{
		
		int currentLv = getNodeCodeLv(nodeCode);
		//只有1层的，就是自身顶层节点
		if (currentLv == 0)
		{
			return nodeCode;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("10");

		sb.append(trailingNine(currentLv));

		return Double.parseDouble(sb.toString());
		

//		String nodeCodeStr = String.valueOf(nodeCode);
//
//		String fromNodeCodeScore = nodeCodeStr.substring(0, nodeCodeStr.length() - 2) + trailingNine(1);
//
//		return Double.parseDouble(fromNodeCodeScore);
	}
	
	
	/**
	 * 判断 sourceNodeCode 是不是 compareNodeCode的子节点。
	 * 
	 * @param sourceNodeCode
	 * @param compareNodeCode
	 * @return
	 */
	public static boolean isSubNode(long sourceNodeCode,long compareNodeCode) {
		
		//sourceNodeCode如果是 compareNodeCode子节点，那么nodecode肯定 sourceNodeCode>compareNodeCode
		if(sourceNodeCode <= compareNodeCode) {
			return false;
		}
		
		return String.valueOf(sourceNodeCode).startsWith(String.valueOf(compareNodeCode));
	}
	
	
	
	/**
	 * 判断 sourceNodeCode 是不是 compareNodeCode 的父节点.
	 * 
	 * @param sourceNodeCode
	 * @param compareNodeCode
	 * @return
	 */
	public static boolean isFatherNode(long sourceNodeCode,long compareNodeCode) {
		
		//sourceNodeCode如果是 compareNodeCode 父节点，那么nodecode肯定 sourceNodeCode<compareNodeCode
		if(sourceNodeCode >= compareNodeCode) {
			return false;
		}
		
		return String.valueOf(compareNodeCode).startsWith(String.valueOf(sourceNodeCode));
	}
	
	
	
	/**
	 * 判断这个nodecode的父节点，在不在map里，不包含本身.
	 * 
	 * @param nodeCode
	 * @param map nodecode为key
	 * @return
	 */
	public static  boolean isParentNodeInMap(long nodeCode,Map<String,String> map) {
		
		//查询当前的lv
		int  currentlv = TreeNodeUtils.getNodeCodeLv(nodeCode);
		
		//
		if(currentlv ==0 || currentlv == 1) {
			return false;
		}
		
		String nodeCodeStr = String.valueOf(nodeCode);
		
		//当前级别
		for(int i = 1; i< currentlv;i++) {
			
			//因为有第0层，为10，所以是 (1+i)*2-1,比如第一层，取0-3，第2层 0-5
			int endIndex = (1+i)*2-1;
			
			String nodeCodeTmp = nodeCodeStr.substring(0, endIndex);
			
			//看看父节点在不在这个map里
			String val = map.get(nodeCodeTmp);
			
			if(StringUtils.isNotBlank(val)) {
				return true;
			}
			
		}
		
		return false;
	}
	
	
	
	/**
	 * 根据nodecode查找父节点的nodecode
	 * @param nodeCode
	 * @return
	 */
	public static  List<Long> getParentNodeCodes(long nodeCode)
	{

		String nodeCodeStr = String.valueOf(nodeCode);

		int length = nodeCodeStr.length();

		// 2位或者2位以下，没有父节点
		if (length <= LV_DISTANCE)
		{
			return null;
		}

		List<Long> list = new ArrayList<>();

		for (int i = 2; i < length; i = i + 2)
		{

			// 从头开始截取
			String subStr = nodeCodeStr.substring(0, i);

			list.add(Long.parseLong(subStr));
		}

		return list;
	}

}