<?xml version="1.0" encoding="UTF-8"?>
<html keyboard="adjust">
	<head>
		<title show="false" />
		<link rel="stylesheet" href="res:css/baseTitle.css" type="text/css" />
		<link rel="stylesheet" href="res:css/common.css" type="text/css" />
		<style type="text/css">

		</style>
		<script type="text/javascript"><![CDATA[ 
			var para = "${para}";			
			function accept(){
				window.open("res:page/report_loc.xhtml", false, false, "", "param="+para);				
			}
			var AJAX_HEADER = '{ "Content-Type":"application/x-www-form-urlencoded; charset=utf-8","Accept-Language":"zh-cn"}';
			function refuse(){
				var url = "http://IORDER_URL/app/activelocation/report.action";
				var paraArray = para.split(",");
				if(paraArray.length==4){
					var param = "tenantId="+paraArray[1];				
					param +="&locRst.userId="+paraArray[2];	
					param +="&locRst.messageId="+paraArray[3];	
					param +="&locRst.status=0";
					param +="&locRst.resultMessage=拒绝定位！";
					param +="&locRst.esn="+Util.getEsn();
					param +="&locRst.imsi="+Util.getImsi();
					var ajaxObj = new Ajax(url,"POST",param,refuseCallback,refuseCallback,AJAX_HEADER,false);
					ajaxObj.send();	
				}else{
					alert("参数不正确:"+para);
				}
			}
			 function refuseCallback(ajax){					
				close();
			 }
		]]></script>
	</head>
	<header class="header">
		<div class="header-left">
			<img src="res:image/head_back.png" clicksrc="res:image/head_back_c.png" discache="false" href="script:close" />
		</div>
		<div class="header-center">
			<font class="titleFont">消息提醒</font>
		</div>
		<div class="header-right">			
		</div>
	</header>
	<body>
		<br/>
		
		<div class="round_top"></div>
		<div class="round_middle" >
			<div >
				${username}想知道您当前所在位置，您同意上报您的位置信息吗？
			</div>
		</div>
		<div class="round_bottom"></div>
		
		<br size="20"/>

		<div style="width:50%;text-align:center;">
			<input type="button" value="同意" onclick="javascript:accept();" style="height:30;"/>
		</div>
		<div style="width:50%;text-align:center;">
			<input type="button" value="拒绝" onclick="javascript:refuse();"  style="height:30;"/>
		</div>
		<br/>
	</body>
	<menubar show="false" />
</html>