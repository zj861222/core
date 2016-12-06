<%-- 
    Document   : jasper
    Created on : 2009-9-28, 9:38:41
    Author     : edwang
--%>

<%@tag description="report tag file" pageEncoding="UTF-8"%>
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

<%
    //下述代码，用来取得实际文件所在的文件夹，计算出 basePath
    //设定正确的 base 之后，dreamweaver 也可以识别
    //取得应用所在的目录
    String path = request.getContextPath();
    //取得文件所在的文件夹，并且取得最后一个 "/" 的部分
    String path1 = request.getServletPath();
    int i = path1.lastIndexOf("/");
    String path2 = path1.substring(0,i);
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
    <style>
<!--
    img{cursor:hand;}
-->
    </style>
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
       
        function onQuery(ipage){
            if(ipage == null || ipage == undefined) ipage = 0;
            submitRequest("<%= ctxPath%>/servlets/qryreport?spage="+ipage,"showReportFrame", "html");
        }

        function onReload(){
            alert("onReload() is called!");
        }

        function onPrint(){
           if(showReportFrame.document.body.innerHTML.length == 0) 
           {
               fh.alert("没找到要打印的报表，请查询后打印！");
               return;
           }
           showReportFrame.print();
        }

        function onExportExcel(){
            
            if(bodyLength()==0){
                
                fh.alert("没找到要导出的报表，请查询后导出！");
            }else{
                
                submitRequest("<%= ctxPath%>/servlets/qryreport","", "xls");
            }
        }

        function onExportPDF(){
            
            if(bodyLength()==0){
                
                fh.alert("没找到要导出的报表，请查询后导出！");
            }else{
                
                submitRequest("<%= ctxPath%>/servlets/qryreport","", "pdf");
            }
        }

        function onExportWord(){
            
            if(bodyLength()==0){
                
                fh.alert("没找到要导出的报表，请查询后导出！");
            }else{
                
                submitRequest("<%= ctxPath%>/servlets/qryreport","", "doc");
            }
        }
        
        function bodyLength(){
        
            return showReportFrame.document.body.innerHTML.length;
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
            return checkForm();
        }
        
        /*
         * 主要区域高度自适应
         */
        function resize(){
            var findToolbarHeight = document.getElementById("queryForm").offsetHeight;
            var conHeight = windowHeight() - findToolbarHeight - 70; 
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
            window.attachEvent("onload", resize);
            window.attachEvent("onload", openQuery); 
        }else{  //W3C
            //window.addEventListener("resize", resize, false);
            window.addEventListener("load", resize, false);
            window.addEventListener("load", openQuery, false);  
        }
        
        function startWait(){
            document.getElementById("slitbar").style.display="block";
            document.body.style.cursor = "wait";
        }
        
        function endWait(){
            document.getElementById("slitbar").style.display="none";
            document.body.style.cursor = "";
        }
    </script>

    <div id="queryForm">
        <form name="reportForm" id="reportForm" action="" onsubmit="return onQuery();">
            <input type="hidden" name="raq" value="<%= reportConfigName%>" />
            <input type="hidden" name="format" value="html" />
            <jsp:doBody />
        </form>
        
    </div>
    <!-- 等待界面 -->
    <link rel="stylesheet" type="text/css"  href="/common/loading/style.css" />
    <div id="slitbar" style="width: 100%; height: 100%;">
        <div class="loading-div">
            <div class="loading-indicator">
                数据加载中，请稍候...
            </div>
        </div>
    </div>
    <div id="showReportDiv" height="16">
        <div class="reportbox">
            <iframe onload ="endWait();" name="showReportFrame" id="showReportFrame" height="100%" width="100%"  border="0" frameborder="0">
            </iframe>
        </div>
        <div class="pagebox">
        <div class="pagebox_inner">
            <table width="100%">
            <tbody id="bars" style="display:none">
                <tr height="12">
                    <td align="right"><span id="controlBar" align="left" width="60%"></span></td>
                </tr>
            </tbody>
            </table>
        </div>
        </div>
    </div>
<script type="text/javascript">
<!--
function checkEditor(obj) {
    var key = document.getElementById(obj.name+"_key");
    if(obj.value.length > 0) {
        key.style.display='none';
    }
    else {
        key.style.display='';
    }
}

function checkForm() {
    var objs = document.images;
    for(i = 0;i< objs.length;i++) {
        if(objs[i].id.indexOf("img") == 0) {
            var inTemp = objs[i].id.substr(3);
            var inObj = document.getElementById(inTemp);
            if(inObj == undefined) {
                return false;
            }
            else if(inObj.value.length == 0){
                //alert(inObj.name);
                var msg = "必填选项不能为空!";
                if(inObj.name == "P1_STATYEAR"){
                    msg = "统计年份不能为空！";
                }else if(inObj.name == "P1_STATMONTH"){
                    msg = "统计月份不能为空！";
                } else if(inObj.name == "P1_STARTDATE"){
                    msg = "开始日期不能为空!";
                } else if(inObj.name == "P1_ENDDATE"){
                    msg = "结束日期不能为空！";
                }    /*
                } else if(inObj.name == "P1_DATATYPE"){
                   msg = "库存指标不能为空!";
                }*/
                
                fh.alert(msg,null,null);
                inObj.focus();
                return false;
            }
            else
            {
                if(inObj.name == "P1_STARTDATE"){
                    var oEnd = document.getElementById("_P1_ENDDATE");
                    var sStart = inObj.value;
                    var sEnd  = oEnd.value;
                    var time1 = new Date(sStart.replace(/-/g,"/"));
                    var time2 = new Date(sEnd.replace(/-/g,"/"));
                    var days = (time2.getTime() - time1.getTime()) / 86400000;
                    var reportId = $("#reportId").val();
                    if (days < 0 )
                    {
                        alert("开始日期必须小于结束日期！");
                        return false;
                    }
                    else if(reportId == "6981219297845987084" && days > 30)
                    {
                        alert("查询时间段必须小于31天！");
                        return false;
                    }
                }
                
            }
        }
    }
    return true;
}

function makePage(ipage,tpage) {
    var oo = document.getElementById("bars");
    var opage = document.getElementById('controlBar');
    var html = "";
    if(ipage == undefined) ipage = 0;
    if(tpage == undefined) tpage = 1;
    if(tpage <= 1) {
        html = "<span disabled='disabled' style='color:#7D7C7A'>首页&nbsp;上一页&nbsp;下一页&nbsp;尾页</span>&nbsp;&nbsp;第1页/共1页";
    }
    else {
        //
        if((ipage+1) == 1) {
            html = "首页&nbsp;上一页&nbsp;<a href='javascript:void(0);' onclick='onQuery("+(ipage+1)+");'>下一页</a>&nbsp;<a href='javascript:void(0);' onclick='onQuery("+(tpage-1)+");'>尾页</a>&nbsp;,&nbsp;第"+(ipage+1)+"页/共"+tpage+"页";
        }
        else if((ipage+1) < tpage) {
            html = "<a href='javascript:void(0);' onclick='onQuery(0);'>首页</a>&nbsp;<a href='javascript:void(0);' onclick='onQuery("+(ipage-1)+");'>上一页</a>&nbsp;<a href='javascript:void(0);' onclick='onQuery("+(ipage+1)+");'>下一页</a>&nbsp;<a href='javascript:void(0);' onclick='onQuery("+(tpage-1)+");'>尾页</a>&nbsp;,&nbsp;第"+(ipage+1)+"页/共"+tpage+"页";
        }
        else {
            html = "<a href='javascript:void(0);' onclick='onQuery(0);'>首页</a>&nbsp;<a href='javascript:void(0);' onclick='onQuery("+(ipage-1)+");'>上一页</a>&nbsp;下一页&nbsp;尾页&nbsp;,&nbsp;第"+(ipage+1)+"页/共"+tpage+"页";
        }
    }
    opage.innerHTML = html;
    oo.style.display = "";
}
//-->
</script>