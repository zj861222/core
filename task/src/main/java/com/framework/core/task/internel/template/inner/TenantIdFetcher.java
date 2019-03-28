package com.framework.core.task.internel.template.inner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSONObject;
import com.framework.core.common.utils.HttpUtil;
import com.framework.core.task.internel.exception.MasterException;
import com.framework.core.task.internel.utils.DesUtils;
import com.framework.core.task.internel.utils.PropertiesUtil;

/**
 * 
 * 获取企业id
 * 
 * @author zhangjun
 *
 */

public class TenantIdFetcher
{

	private static Logger logger = LoggerFactory.getLogger(TenantIdFetcher.class);

	private String PATH = "/schedules/task/getTaskRange.action";

	private String MATER_DOAMIN_KEY = "master.domain.inner.url";

	/**
	 * 独立部署的企业ids key，  没填表示不是独立部署的 ；有值的话，格式是 123,1234,234,逗号隔开
	 */
	private String INDEPEN_DEPLOY_TENANDS_KEY = "independent.deploy.tenants";

	// 独立部署的企业ids key， 没填表示不是独立部署的 ；有值的话，格式是 123,1234,234,逗号隔开
	private String isDebugMode = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, "core.debug.enable", "0");

	private String CODE_SUCCESS = "1";

	// job不运行了
	private String CODE_STOP_JOB = "2";
	
	private static final int timeout = 10*1000;

	private static final String DES_KEY = "waiqin365_auth";

	/**
	 * 
	 */
	private String DEFAULT_MASTER_DOMAIN = "http://internal-master-452572142.cn-north-1.elb.amazonaws.com.cn";

	public Pair<Boolean,List<Long>> fetchWaitExecutTenantIds(String jobName, int batchSize, int pageNum) throws MasterException
	{

		return isDebugMode.equals("1") ? getTestTenantIds(pageNum)
				: fetchWaitExecutTenantIdsFromMaster(jobName, batchSize, pageNum);

	}

	/**
	 * 从master 分页获取哪些企业要执行这个job
	 * 
	 * master接口文档地址:
	 * http://172.31.3.113:8080/workspace/myWorkspace.do?projectId=57#398
	 * 
	 *  
	 * @param jobName
	 * @param batchSize
	 * @param pageNum
	 * @return
	 */
	private Pair<Boolean,List<Long>> fetchWaitExecutTenantIdsFromMaster(String jobName, int batchSize, int pageNum)
		throws MasterException
	{

		// 独立部署的企业ids key， 没填表示不是独立部署的 ；有值的话，格式是 123,1234,234,逗号隔开
		String indepTenantIds = PropertiesUtil.getProp(PropertiesUtil.FILE_CORE_CONFIG, INDEPEN_DEPLOY_TENANDS_KEY);

		
		JSONObject jsonObj = new JSONObject();

		jsonObj.put("page", String.valueOf(pageNum));
		jsonObj.put("rows", String.valueOf(batchSize));
		jsonObj.put("task_code", jobName);
		
		//独立部署的企业id,有值的话，格式是 123,1234,234,逗号隔开

		jsonObj.put("indep_tenantIds", indepTenantIds);

		String url = getMasterUrl();

		try
		{

			DesUtils des = new DesUtils(DES_KEY);// 自定义密钥

			String security = des.encrypt(jsonObj.toJSONString(), "utf-8");

			List<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();

			pairs.add(new BasicNameValuePair("digest", security));

			// {"data":{"start_time":"21:00","tenants":[{"id":"1000000201304120032"},{"id":"5672236347883326796"}]},"code":"1"}
			
			String response =HttpUtil.httpClientPost(url, pairs, timeout);
			

			logger.info("fetch execute job tenant ids master ,[jobName]" + jobName + ",[batchSize]:" + batchSize
					+ ",[pageNum]:" + pageNum + ",response is:" + response);

			List<Long> tenantIds = parseResponse(jobName, response);


			// 不是独立部署的企业，
			if (StringUtils.isEmpty(indepTenantIds))
			{
                //如果tenantIds 为空，那么不需要继续轮训，否则继续
				return CollectionUtils.isEmpty(tenantIds)?Pair.of(false, tenantIds):Pair.of(true, tenantIds);
				
			}

			// 解析独立部署的企业参数，123,1234,234
			String[] tenantIdsstring = indepTenantIds.trim().split(",");
			Assert.isTrue(tenantIdsstring != null && tenantIdsstring.length > 0);

			Set<String> indepTenantIdsSet = new HashSet<>();

			// 放到set里
			for (String tenantId : tenantIdsstring)
			{
				if (StringUtils.isNotBlank(tenantId))
				{
					indepTenantIdsSet.add(tenantId.trim());
				}
			}

			List<Long> resultList = new ArrayList<>();

			for (Long tenantId : tenantIds)
			{
				if (indepTenantIdsSet.contains(tenantId.toString()))
				{
					resultList.add(tenantId);
				}
			}

			//独立部署这种情况，即使resultList为空，也要继续轮训
			return Pair.of(true, resultList);

		}
		catch (MasterException exception)
		{

			logger.error("fetch execute job tenant ids master exception,[jobName]" + jobName + ",[batchSize]:"
					+ batchSize + ",[pageNum]:" + pageNum, exception);
			throw exception;
		}
		catch (Exception e)
		{
			logger.error("fetch execute job tenant ids unexpected exception,[jobName]" + jobName + ",[batchSize]:"
					+ batchSize + ",[pageNum]:" + pageNum, e);

			throw new MasterException(jobName, e);
		}

	}

	/**
	 * 获取master url
	 * 
	 * @return
	 */
	private String getMasterUrl()
	{

		String masterDomain = PropertiesUtil
				.getProp(PropertiesUtil.FILE_CORE_CONFIG, MATER_DOAMIN_KEY, DEFAULT_MASTER_DOMAIN).trim();

		return masterDomain + PATH;

	}

	/**
	 * 解析response
	 * 
	 * @param jobName
	 * @param response
	 * @return
	 * @throws MasterException
	 */
	private List<Long> parseResponse(String jobName, String response) throws MasterException
	{

		// code 响应码 string 0-失败 1-成功
		// data object
		// start_time 开始执行时间 string
		// tenants array<object>
		// id 企业ID string
		// message 响应信息 string

		// 如果response为空，表示网络异常
		if (StringUtils.isEmpty(response))
		{
			throw new MasterException(jobName);
		}

		JSONObject obj = JSONObject.parseObject(response);

		// 数据异常，直接中断查询
		if (obj == null)
		{
			throw new MasterException(jobName);
		}

		String code = obj.getString("code");

		// job停止
		if (CODE_STOP_JOB.equals(code))
		{
			return null;
		}

		// 请求不是成功,并且没有停止job
		if (!CODE_SUCCESS.equals(code))
		{
			throw new MasterException(jobName);
		}

		String data = obj.getString("data");

		if (StringUtils.isEmpty(data))
		{
			throw new MasterException(jobName);
		}

		JSONObject dataObj = JSONObject.parseObject(data);

		if (dataObj == null)
		{
			throw new MasterException(jobName);
		}

		String tenantIds = dataObj.getString("tenants");

		if (StringUtils.isEmpty(tenantIds))
		{
			return null;
		}

		List<String> idsList = JSONObject.parseArray(tenantIds, String.class);

		if (CollectionUtils.isEmpty(idsList))
		{
			return null;
		}

		List<Long> ids = new ArrayList<>();

		for (String tenantId : idsList)
		{

			JSONObject tenantObj = JSONObject.parseObject(tenantId);

			if (tenantObj != null)
			{
				String id = tenantObj.getString("id");
				ids.add(Long.parseLong(id));

			}

		}

		return ids;

	}

	private Pair<Boolean,List<Long>> getTestTenantIds(int pageNum)
	{

		if (pageNum == 1)
		{
			List<Long> ids = new ArrayList<>();

			for (long i = 1000; i < 1001; i++)
			{
				ids.add(i);

			}

			return Pair.of(true, ids);
		}
		else
		{
			return Pair.of(false, null);
		}
	}
	
	
	
	


}