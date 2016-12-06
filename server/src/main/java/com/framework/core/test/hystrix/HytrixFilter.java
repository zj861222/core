//package com.framework.core.test.hystrix;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.concurrent.Future;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletOutputStream;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.apache.commons.io.IOUtils;
//import org.apache.struts2.ServletActionContext;
//
//import com.framework.core.common.exception.BizException;
//import com.netflix.hystrix.HystrixCommand;
//import com.netflix.hystrix.HystrixCommandGroupKey;
//import com.netflix.hystrix.HystrixCommandKey;
//import com.netflix.hystrix.HystrixCommandProperties;
//import com.netflix.hystrix.HystrixCommand.Setter;
//
//
//public class HytrixFilter implements Filter
//{
//
//	@Override
//	public void init(FilterConfig filterConfig) throws ServletException
//	{
//
//	}
//
//	@Override
//	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
//		ServletException
//	{
//		
////		chain.doFilter(request, response);
//		
//		TxCommand cmd = new TxCommand((HttpServletRequest)request, (HttpServletResponse)response, chain, 11L, "appsvr");
//		
//		
//		cmd.execute();
//		
////		chain.doFilter(request, response);
//////		        HytrixGetCommand<T> command = new HytrixGetCommand<>(restTemplate, url, serviceName, requestParams, responseType);
//////		        command.setFallback(fallback);
//////		        command.setAudit(this.serviceCallAudit);
//////		        command.setSrcServiceName(ThreadProfileInterceptor.getServiceName());
//////		        Future<T> future = command.queue();
////		
////				
////				
////		chain.doFilter(request, response);
//
//	}
//
//	@Override
//	public void destroy()
//	{
//
//	}
//	
//	
//	
//	private class TxCommand extends HystrixCommand<String> {
//
//		
//		protected final ServletRequest request;
//
//		protected final ServletResponse response;
//
//		private String serviceName;
//
//		private String url;
//
//		private FilterChain chain;
//
//		private HttpServletResponse fallback;
//
//
//		private Long tenantId;
//
//		/**
//		 * 源自那个服务
//		 */
//		private String srcServiceName;
//		
//		public TxCommand(ServletRequest request, ServletResponse response, FilterChain chain,
//				Long tenantId, String context) {
//			
//			super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(context))
//					.andCommandKey(HystrixCommandKey.Factory.asKey(((HttpServletRequest)request).getRequestURI()))
//					.andCommandPropertiesDefaults(  
//                            // we default to a 100ms timeout for secondary  
//                            HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1000))  
//					
//					);
//
//			
//			this.srcServiceName = null;
//
//			this.serviceName = ((HttpServletRequest)request).getRequestURI();
//
//			this.chain = chain;
//
//			this.tenantId = tenantId;
//
//			this.request = request;
//
//			this.response = response;
//		}
//
//		@Override
//		protected String run() throws Exception {
//			
//			System.out.println("is in run()");
//
//			
//			
//			     chain.doFilter(request, response);
//		
//			return null;
//		}
//		
//		
//		
//	    @Override
//	    public String getFallback() {
//	    	
//	    	try {
//	    	fetchTokenInvalidMessage((HttpServletResponse)response,"fail");
//	    	}catch(Exception e) {
//	    		e.printStackTrace();
//	    	}
//
//	    	
//	    	return null;
//	
//	    }
//	    
//	    
//	    
//	    
//		protected void fetchTokenInvalidMessage(HttpServletResponse response, String context) {
//
//			try {
//				response.setContentType("application/uixml+xml");
//				response.setCharacterEncoding("UTF-8");
//				PrintWriter out = response.getWriter();
//				out.println("<html type='alert'><body><alert title='信息提示' icontype='error'><msg>");
//				out.println(context);
//				out.println("</msg>");
//				out.println("<nextaction href='script:reloadapp'></nextaction>");
//				out.println("</alert></body></html>");
//				out.flush();
//				out.close();
//			} catch (IOException ex) {
//			}
//
//		}
//		
//	}
//
//}
