package com.waiqin365.search;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.framework.core.error.exception.BizException;
import com.framework.core.search.index.builder.IndexBuilder;
import com.framework.core.search.model.IndexData;


@Component
public class BrandIndexBuilder extends IndexBuilder
{

	@Override
	public List<IndexData> queryBizDataByPage(long offset, long pageSize)
	{

		long pageNum = offset / pageSize + 1;

		if (pageNum > 0 && pageNum <= 20)
		{

			List<IndexData> list = new ArrayList<>();

			for (long i = offset; i < offset + pageSize; i++)
			{
				IndexData data = new BrandDataTest(i, "b" + i, "b" + i, "b" + i);
				list.add(data);

			}

//			if (pageNum == 2)
//			{
//				throw new BizException(11111111, "异常");
//			}

			return list;

		}

		return null;
	}

	@Override
	public long countBizData()
	{
		return 10;
	}

	@Override
	public long getBizPageSize()
	{

		return 5;
	}

}