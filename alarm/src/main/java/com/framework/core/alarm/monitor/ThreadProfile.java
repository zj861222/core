package com.framework.core.alarm.monitor;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录线程执行路径
 * 
 * @author zhangjun
 *
 */
public class ThreadProfile
{

	/**
	 * 打印调试日志
	 */
	private static final Logger logger = LoggerFactory.getLogger(ThreadProfile.class);

	/**
	 * 调用堆栈信息记录，每次在start和stop时，都进行清理，避免出现引用仍然存在的问题
	 */
	private static final ThreadLocal<StackTrace> stackTrace = new InheritableThreadLocal<StackTrace>();

	/**
	 * 调用响应过长统计，默认情况下，超过300ms响应的请求会被输出到日志中
	 */
	private static int threadshold = 300;

	/**
	 * 线程统计开始标记，每次在start和stop时，都进行清理，避免出现引用仍然存在的问题
	 */
	private static final ThreadLocal<Boolean> isStart = new InheritableThreadLocal<Boolean>();

	/**
	 * 堆栈最大层数
	 */
	private static final int MAX_STACK_NUM = 50;

	/**
	 * 开始统计当前线程的执行延时
	 * @param message
	 */
	public static void start(String message, int threadshold)
	{

		// 调用的 response time （线程执行的时间）超过 threadshold时，打印错误日志，并dump堆栈
		ThreadProfile.threadshold = threadshold;

		// 避免异常情况未清理，造成内存泄露
		stackTrace.remove();

		// 创建当前线程的调用堆栈实例，用于记录关联路径的调用情况，关键路径通过全局的interceptor进行拦截
		stackTrace.set(new StackTrace(message));

		isStart.set(new Boolean(true));
	}

	/**
	 * 结束统计，并打印结果, 整个调用的延时
	 */
	public static Pair<String, Long> stop()
	{

		// runLogger.warn("stop:"+System.currentTimeMillis());

		if (stackTrace.get() == null)
		{
			return null;
		}

		/**
		 * 记录线程执行结束时间
		 */
		stackTrace.get().end();

		/**
		 * 记录延时，返回监控profile信息的filter
		 */
		long duration = stackTrace.get().duration();

		/**
		 * dump调用堆栈
		 */
		String dumpStack = stackTrace.get().dump();

		// 结束标记
		isStart.remove();
		// 清理threadlocal引用
		stackTrace.remove();

		return Pair.of(dumpStack, duration);
	}

	/**
	 * 方法进入时记录线程的执行堆栈信息，拦截器调用
	 * @param className
	 * @param methodName
	 */
	public static void enter(String className, String methodName)
	{
		// 如果已经标记为开始，并且 stacktrace实例已经创建，则进行统计
		if ((isStart.get() != null) && (stackTrace.get() != null))
		{
			stackTrace.get().enter(new Entry(className, methodName, "enter"));
		}
	}

	/**
	 * 方法return时记录线程的执行堆栈信息，拦截器调用
	 */
	public static void exit()
	{
		// 如果已经标记为开始，并且 stacktrace实例已经创建，则进行统计
		if ((isStart.get() != null) && (stackTrace.get() != null))
		{
			stackTrace.get().exit();
		}
	}

	/**
	 * 当前线程的线程的堆栈信息
	 * @author 
	 *
	 */
	public static final class StackTrace
	{

		private List<Entry> entryList = new ArrayList<Entry>();
		Stack<Entry> entryStack = new Stack<Entry>();

		/**
		 * 当前线程的描述信息，timer filter调用时为请求的url
		 */
		private String message;

		private long beginTime;

		private long endTime;

		// 栈的深度限制
		private long limitStackLevel;

		/**
		 * 当前处于堆栈的层次
		 */
		private int currentStackLevel;

		/**
		 * 线程执行开始
		 * @param message
		 */
		public StackTrace(String message)
		{
			this.beginTime = System.currentTimeMillis();
			this.endTime = beginTime;
			this.message = message;
			this.currentStackLevel = 0;
		}

		/**
		 * 记录方法的进入
		 * @param entry
		 */
		public void enter(Entry entry)
		{

			entry.level = ++this.currentStackLevel;

			// 放到堆栈里，为了exit pop出来时能因为引用修改到list里的entry
			entryStack.push(entry);

			if (entry.level > limitStackLevel && limitStackLevel > 0)
			{
				return;
			}

			int size = entryList.size();

			if (size < MAX_STACK_NUM)
			{
				entryList.add(entry);
			}
			else
			{
				limitStackLevel = entry.level - 1;
			}

		}

		/**
		 * 记录方法的返回
		 */
		public void exit()
		{
			this.currentStackLevel--;
			// 引用，修改了list里的结束时的 exitTimestamp
			Entry entryPop = (Entry) entryStack.pop();
			entryPop.exitTimestamp = System.currentTimeMillis();
		}

		/**
		 * 线程执行结束
		 */
		public void end()
		{
			endTime = System.currentTimeMillis();
		}

		/**
		 * 线程执行延时
		 * @return
		 */
		public long duration()
		{
			return endTime - beginTime;
		}

		public String dump()
		{
			long dur = this.duration();
			if (dur > ThreadProfile.threadshold)
			{

				StringBuffer sb = new StringBuffer();
				sb.append("Total Delay [").append(duration()).append("ms] ").append(this.message).append("\n");

				for (Entry entry : entryList)
				{
					if (entry == null)
					{
						continue;
					}

					// 超过堆栈长度限制的不打印
					if (entry.level > limitStackLevel && limitStackLevel > 0)
					{
						continue;
					}

					for (int i = 0; i < entry.level; i++)
					{
						sb.append("    ");
					}
					sb.append(entry.toString()).append("\n");
				}

				// // 大于300毫秒的，才需要warn
				// if(dur > 300) {
				// runLogger.debug("Response time [{}] exceed threadshold [{}],
				// request [{}], stack info:\n[{}]\n\n\n",
				// this.duration(), ThreadProfile.threadshold, this.message,
				// sb.toString());
				//
				//
				// }

				return sb.toString();
			}

			logger.debug("Response time [{}], request [{}], stack info\n[{}]", this.duration(), this.message,
					entryList.toString());
			return null;
		}

	}

	public static final class Entry
	{
		public String className;
		public String method;
		public long enterTimestamp;
		public String event;
		public int level;
		public long exitTimestamp;

		public Entry(String className, String method, String event)
		{
			this.className = className;
			this.method = method;
			this.enterTimestamp = System.currentTimeMillis();
			this.exitTimestamp = this.enterTimestamp;
			this.event = event;
			this.level = 0;
		}

		public String toString()
		{

			StringBuffer sb = new StringBuffer();

			// +---[127251ms] -
			// com.framework.core.alarm.monitor.interceptor.ThreadProfileInterceptor4Struts.doPreHandle
			// - [enter:1536905498191,exit:1536905625442]
			sb.append("+---[").append(this.exitTimestamp - this.enterTimestamp).append("ms] - ").append(className)
					.append(".").append(method).append(" - [enter:").append(this.enterTimestamp).append(",exit:")
					.append(this.exitTimestamp).append("]");

			return sb.toString();

		}
	}
}
