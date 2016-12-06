package com.framework.core.zookeeper.util;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.framework.core.error.exception.BizException;
import com.framework.core.zookeeper.exception.ZookeeperErrorCode;
import com.framework.core.zookeeper.model.CreateMode;

/**
 * 
 * @author zhangjun
 *
 */
public class ZookeeperClientHelper implements InitializingBean {

	private final static Logger logger = LoggerFactory.getLogger(ZookeeperClientHelper.class);

	private CuratorFramework curatorFramework;

	private static ZookeeperClientHelper instance;

	public CuratorFramework getCuratorFramework() {
		return curatorFramework;
	}

	public void setCuratorFramework(CuratorFramework curatorFramework) {
		this.curatorFramework = curatorFramework;
	}

	@Override
	public void afterPropertiesSet() throws Exception {

		instance = this;
		instance.curatorFramework = this.curatorFramework;

	}

	/**
	 * 创建节点
	 * 
	 * @param path
	 *            路径
	 * @param createMode
	 *            创建的类型，详情见枚举的描述
	 * @param data
	 *            数据，如果传入对象，请先用fastjson序列化
	 */
	public static void createNode(String path, CreateMode createMode, String data) throws BizException {

		instance.checkIsStart();

        //如果节点已存在
		if(isNodeExist(path)) {
			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NODE_EXIST.getCode());
		}
		
		try {
			instance.curatorFramework.create().creatingParentContainersIfNeeded().withMode(CreateMode.findByValue(createMode.getValue())).forPath(path,
					data.getBytes());
			
		} catch (NodeExistsException e) {

			logger.error(
					"ZookeeperClientHelper-create node Data failed,node is  exist, path is {}, createmode is {},data is {}",
					path, createMode.getValue(), data);

			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NODE_EXIST.getCode(), e);

		} catch (Exception e) {
			logger.error("ZookeeperClientHelper-createNode failed,path is {},createMode is {},data is {}", path,
					createMode.getValue(), data);
			throw new BizException(ZookeeperErrorCode.EX_ZK_CREATE_NODE_FAIL.getCode(), e);
		}

	}

	/**
	 * 查询节点数据
	 * 
	 * @param path
	 * @return
	 * @throws BizException
	 */
	public static String getData(String path) throws BizException {

		instance.checkIsStart();
		
        //如果节点不存在
		if(!isNodeExist(path)) {
			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NOT_EXIST.getCode());
		}

		try {
			return new String(instance.curatorFramework.getData().forPath(path));

		} catch (NoNodeException e) {

			logger.error("ZookeeperClientHelper-get node Data failed,node is not exist,path is {} ", path);

			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NOT_EXIST.getCode(), e);

		} catch (Exception e) {
			logger.error("ZookeeperClientHelper-get node Data failed,path is {} ", path);
			throw new BizException(ZookeeperErrorCode.EX_ZK_GET_NODE_DATA_FAIL.getCode(), e);
		}
	}

	/**
	 * 修改节点数据
	 * 
	 * @param path
	 *            节点path
	 * @param data
	 *            数据，如果传入对象，请先用fastjson序列化
	 * @throws BizException
	 */
	public static void updateNodeDate(String path, String data) throws BizException {

		instance.checkIsStart();
		
        //如果节点不存在
		if(!isNodeExist(path)) {
			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NOT_EXIST.getCode());
		}

		try {
			instance.curatorFramework.setData().forPath(path, data.getBytes());
		} catch (NoNodeException e) {

			logger.error("ZookeeperClientHelper-update node Data failed,node is not exist, path is {}, data is {}",
					path, data);

			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NOT_EXIST.getCode(), e);

		} catch (Exception e) {
			logger.error("ZookeeperClientHelper-update node Data failed,path is {},data is {} ", path, data);
			throw new BizException(ZookeeperErrorCode.EX_ZK_UPDATE_NODE_DATA_FAIL.getCode(), e);
		}
	}

	/**
	 * 删除节点
	 * 
	 * @param path
	 * @throws BizException
	 */
	public static void deleteNode(String path) throws BizException {

		instance.checkIsStart();
		
        //如果节点不存在
		if(!isNodeExist(path)) {
			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NOT_EXIST.getCode());
		}

		try {
			instance.curatorFramework.delete().forPath(path);

		} catch (NoNodeException e) {

			logger.error("ZookeeperClientHelper-delete node Data failed,node is not exist,path is {} ", path);

			throw new BizException(ZookeeperErrorCode.EX_ZK_NODE_FAIL_NOT_EXIST.getCode(), e);
		} catch (Exception e) {
			logger.error("ZookeeperClientHelper-delete node Data failed,path is {}", path);
			throw new BizException(ZookeeperErrorCode.EX_ZK_UPDATE_NODE_DATA_FAIL.getCode(), e);
		}
	}

	/**
	 * 判断节点是否存在
	 * @param path
	 * @return
	 * @throws BizException
	 */
	public static boolean isNodeExist(String path) throws BizException {

		instance.checkIsStart();

		try {
			ExistsBuilder builder = instance.curatorFramework.checkExists();

			Stat stat = builder.forPath(path);

			return stat == null ? false : true;

		} catch (Exception e) {

			logger.error("ZookeeperClientHelper-isNodeExist  failed,path is {}", path);
			throw new BizException(ZookeeperErrorCode.EX_ZK_UNEXPECTED_EX.getCode(), e);
		}

	}

	/**
	 * 检查zk是否启动
	 * 
	 * @throws BizException
	 */
	private void checkIsStart() throws BizException {

		if (instance.curatorFramework == null || instance.curatorFramework.checkExists() == null) {
			throw new BizException(ZookeeperErrorCode.EX_ZK_NOT_START.getCode());
		}
	}

	/**
	 * 判断zk是否启动了
	 * 
	 * @return
	 */
	public static boolean isZookeeperStart() {

		if (instance.curatorFramework == null || instance.curatorFramework.checkExists() == null) {
			return false;
		}

		return true;
	}

}