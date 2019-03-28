//package com.framework.core.test.search;
//
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
//import com.framework.core.search.index.IndexClient;
//import com.framework.core.search.index.builder.IndexBuilder;
//
//
//@Component
//public class BrandIndexBuilder extends IndexBuilder {
//	private final Logger logger = LoggerFactory.getLogger(BrandIndexBuilder.class);
////	@Autowired
////	private BrandService brandService;
//
//	/**
//	 * 获取全量数据，从mysql中获取全量数据
//	 */
//	@Override
//	public boolean buildAll(final String tempIndexName, final String indexName, final IndexClient indexClient)
//			throws Exception {
//		
//		
//		return true;
////		boolean result = true;
////		long begin = System.currentTimeMillis();
////		final String idField = ISearchConstans.getKeyField(indexName);
////		if (idField == null) {
////			logger.info("[{}:mysql-->index]失败, 不存在[name={}]的EsMapping", tempIndexName, indexName);
////			result = false;
////		}
////		// 获取总的记录数，设置每页取1条，因为无需拿数据
////		int totalCount = brandService.count();
////		final int limit = ISearchConstans.SEARCH_INDEX_BATCH_LIMIT;
////		int threadCount = ISearchConstans.SEARCH_INDEX_BATCH_MAX_THREAD_SIZE; // 执行同步的最大线程数量
////		int totalPageSize = (int) ((totalCount - 1) / limit + 1); // 总页数
////		// 最大线程数大于总页数，则需要调整最大线程数
////		if (threadCount > totalPageSize) {
////			threadCount = totalPageSize;
////		}
////		DataSynchronizationDispatcher dispatcher = new DataSynchronizationDispatcher(totalPageSize);
////		DataSynchronizationWorkThread[] dst = new DataSynchronizationWorkThread[threadCount];
////		for (int i = 0; i < dst.length; i++) {
////			dst[i] = new DataSynchronizationWorkThread("" + i, dispatcher, new WorkThreadCallback() {
////				@Override
////				public void execute(Task task) throws Exception {
////					long begin = System.currentTimeMillis();
////					int pageNo = ((DataSynchronizationTask) task).getPageNo();
////					int start = (pageNo - 1) * limit;
////					List<Brand> list = brandService.getBrandPageLists(start, limit);
////					long readFromMysql = System.currentTimeMillis() - begin;
////					if (list != null && list.size() > 0) {
////						indexClient.addIndexDataBean(tempIndexName, list);
////					}
////					logger.info("threadName=" + Thread.currentThread().getName() + " pageNo=" + pageNo + " resultSize="
////							+ list.size() + " fromMysql=" + readFromMysql + " intoIndex="
////							+ (System.currentTimeMillis() - begin - readFromMysql));
////				}
////			});
////			dst[i].start();
////		}
////		for (int i = 0; i < dst.length; i++) {
////			dst[i].join();
////		}
////		if (dispatcher.isCompleted()) {
////			logger.info("[{}:mysql-->index]完毕，耗时:{}ms", tempIndexName, System.currentTimeMillis() - begin);
////		} else {
////			logger.info("[{}:mysql-->index]失败", tempIndexName);
////			result = false;
////		}
////		if (!result) {
////			indexClient.deleteIndex(tempIndexName);
////		}
////		return result;
//	}
//}
