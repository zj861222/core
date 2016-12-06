<%-- 
    Document   : jasper
    Created on : 2009-9-28, 9:38:41
    Author     : edwang
--%>

<%@tag description="报表集成的tag file" pageEncoding="UTF-8"%>
<%@tag import="java.io.File"%>

<%-- The list of normal or fragment attributes can be specified here: --%>
<%@attribute name="uid" required="true"%>
<%@attribute name="report" required="true"%>
<%@attribute name="initparams"%>
<%@attribute name="toolbars"%>
<%@attribute name="cachetime"%>
<%@attribute name="format"%>
<%@attribute name="helpurl"%>
<%@attribute name="openquery"%>
<%@attribute name="listener"%>

<script src="${pageContext.request.contextPath}/js/report.js"></script>
<%-- any content can be specified here e.g.: --%>
<%

        //下述代码，用来取得实际文件所在的文件夹，计算出 basePath
    //设定正确的 base 之后，dreamweaver 也可以识别
   
    //取得应用所在的目录
    String path = request.getContextPath();
    //取得文件所在的文件夹，并且取得最后一个 "/" 的部分
    String path1 = request.getServletPath();
    int i = path1.lastIndexOf("/");
    String path2 = path1.substring(0,i);
    //+ path2 + "/"
    String basePath = request.getScheme() + "://" 
            + request.getServerName() + ":" + request.getServerPort()   
            + path ; 

    // 执行的动作，reload 重新装载 print 打印 exportXls 导出Excel exportPdf 导出pdf exportDoc 导出Word
    String action = request.getParameter("action");
    String ctxPath = basePath;
    String jasperPath = ctxPath  + "/common/jasper";
    
    com.fh.report.jasper.ReportRequestInfo reportConfig = new com.fh.report.jasper.ReportRequestInfo();
    try{
        reportConfig.setUid(uid);
        reportConfig.setReport(report);
        reportConfig.setInitParams(initparams);
    if (cachetime != null || cachetime.length() > 0){
        reportConfig.setCacheTime(Integer.parseInt(cachetime));
    }
    }catch(Exception ex){ex.printStackTrace();}
    
    String reportConfigName = (uid + "|" + report).hashCode() + "";

    session.setAttribute(reportConfigName, reportConfig);
    StringBuffer js = new StringBuffer();   
%>
    <script language="javascript">
        function openQuery() {
        <%
            String encode = request.getCharacterEncoding();
            if (encode == null)
            {
                encode = "ISO8859-1";
            }
            
         // 添加所有的http request parameter
            java.util.Enumeration em = request.getParameterNames();
            String method = request.getMethod();
            while(em.hasMoreElements())
            {
              String name = em.nextElement().toString();
              String value = "";
              
              if ("GET".equalsIgnoreCase(method))
              {
                  value = new String(request.getParameter(name).getBytes(
                          com.fh.report.jasper.j2ee.ReportServlet.REPORTENCODE),encode);
              }
              else
              {
                  value = request.getParameter(name);
              }
              js.append("initInput('").append(name).append("', '").append(value).append("');\n");
            }            
        %>
        	<%= js.toString() %>
        	if ('true' == '${openquery}') {
            	onQuery();
            }
        }
       
        function onQuery(){
            submitRequest("<%= ctxPath%>/servlets/jrreport","showReportFrame", "html");
        }

        function onReload(){
            alert("onReload() is called!");
        }

        function onPrint(){

            submitRequest("<%= ctxPath%>/common/jasper/jsp/print.jsp","_blank","bin");
       
        }

        function onExportExcel(){
            submitRequest("<%= ctxPath%>/servlets/jrreport","", "xls");
        }

        function onExportPDF(){
            submitRequest("<%= ctxPath%>/servlets/jrreport","", "pdf");
        }

        function onExportWord(){
            submitRequest("<%= ctxPath%>/servlets/jrreport","", "docx");
        }

        function onFisrtPage(){
            alert("onFisrtPage is called!");
        }

        function onPrevPage(){
            alert("onPrevPage is called!");
        }

        function onNextPage(){
            alert("onNextPage is called!");
        }

        function onLastPage(){
            alert("onLastPage is called!");
        }

        function onHelp(){
            window.open("<%= ctxPath%>/${helpurl}");
        }
        
        function submitRequest(action,target, format) {
        	var check = checkInput();
            if(check == false){
                return false;
            }
            if(format == "html"){
            	startWait();
            }
            document.reportForm.method = "POST";
            document.reportForm.action = action;
            document.reportForm.format.value = format;
            document.reportForm.target = target;
            document.reportForm.submit();
        }
        
        /*
         * 验证用户输入是否正确
         */
        function checkInput(){
            
        	return checkUtil();
        }
        
        /*
         * 主要区域高度自适应
         */
        function resize(){
            var secondToolbarHeight = document.getElementById("secondToolbar").offsetHeight;  //\u8ba1\u7b97\u5de5\u5177\u680f\u9ad8\u5ea6
            var findToolbarHeight = document.getElementById("queryForm").offsetHeight;  //\u8ba1\u7b97\u641c\u7d22\u680f\u9ad8\u5ea6
            var conHeight = windowHeight() - secondToolbarHeight - findToolbarHeight - 2; 
            document.getElementById("showReportDiv").style.height = conHeight + "px";
            document.getElementById("showReportFrame").height = (conHeight-1) -184 + "px";
            /**
             *弹出窗口的高度计算   
             */
            if(window.parent.id == undefined){
               document.getElementById("showReportFrame").style.height = (conHeight-1) + "px"; 
            }
        }
        
        /*
         * 获得当前窗口高度
         */
        function windowHeight(){
            var de = document.documentElement;
            return  ( de && de.clientHeight) || document.body.clientHeight;
        }
        
        /**
	     * 初始化参数
	     */
	    function initInput(name, value) {
	        var el = document.getElementsByName(name)[0];
	        if (el) {
	            el.value = value;
	        } 
	    }
        
        if(document.attachEvent){ //IE
            //window.attachEvent("onresize", resize);
            //window.attachEvent("onload", resize);
            
            window.attachEvent("onload", openQuery); 
        }else{  //W3C
            //window.addEventListener("resize", resize, false);
            //window.addEventListener("load", resize, false);
            window.addEventListener("load", openQuery, false);  
        }
        
        function startWait(){
            document.getElementById("slitbar").style.display="block";
            document.body.style.cursor = "wait";
        }
        
        function endWait(){
            document.getElementById("slitbar").style.display="none";
        }
        
        
        
    </script>

    <div id="secondToolbar">
        <span>
            <%--
            <img src="<%= jasperPath %>/images/query.gif" onclick="onQuery();" title="Query" />
            
            <img src="<%= jasperPath %>/images/reload.gif" onclick="onReload();" title="Reload" />
            --%>
           
            
            <img src="<%= jasperPath %>/images/excel.gif" onclick="onExportExcel();" style="cursor:pointer;" title="ExportExcel" />
             <img src="<%= jasperPath %>/images/print.gif" onclick="onPrint();" style="cursor:pointer;" title="Print" />
            <%--
            <img src="<%= jasperPath %>/images/pdf.gif" onclick="onExportPDF();" title="ExportPDF" />
            <img src="<%= jasperPath %>/images/doc.gif" onclick="onExportWord();" title="ExportWord" />
            --%>
        </span>
        <%--
        <span>
            <img src="<%= jasperPath %>/images/firstpage.gif" onclick="onFisrtPage();" title="FisrtPage" />
            <img src="<%= jasperPath %>/images/prevpage.gif" onclick="onPrevPage();" title="PrevPage" />
            <img src="<%= jasperPath %>/images/nextpage.gif" onclick="onNextPage();" title="NextPage" />
            <img src="<%= jasperPath %>/images/lastpage.gif" onclick="onLastPage();" title="LastPage" />
        </span>
        
        <img src="<%= jasperPath %>/images/help.gif" onclick="onHelp();" title="Help" />
        --%>
    </div>
    <div id="queryForm">
        <form name="reportForm" id="reportForm" action="" onsubmit="return onQuery()">
            <input type="hidden" name="raq" value="<%= reportConfigName%>" />
            <input type="hidden" name="format" value="html" />
            <jsp:doBody />
        </form>
         
    </div>
    <!-- 滚动条 -->
    <div id="slitbar" style="background:#D8D9FF;border: 1px solid #979897;width:350px;height:90px;display:none;
    z-index:100;position: absolute; bottom: 1in; left: 2.5in; right: 1in; top: 2.5in;font-weight:bold;text-align:center">
        <br>
        <center>
        查询中 请稍后 ...
        </center>
    </div>
    <div id="showReportDiv">
        <iframe onload ="endWait();" name="showReportFrame" id="showReportFrame" height="100%" width="100%"  border="0" frameborder="0">
        </iframe>    
    </div>
    <script type="text/javascript">
        resize();
    </script>
    <div id="printDiv" width="200" height="40">
        
    </div>
</body>