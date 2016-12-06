<?xml version="1.0" encoding="UTF-8"?>
<html>
	<head>
		<title show="false" />
		<link rel="stylesheet" href="res:css/baseTitle.css" type="text/css" />
		<link rel="stylesheet" href="res:css/global.css" type="text/css" />
		<link rel="stylesheet" href="res:css/common.css" type="text/css" />
		<style type="text/css">
		.messageDetailTitle{width:100%;text-align:center;height:auto;font-size:1.3em;}
		.messageForm{width:100%;text-align:center;color:#666666;font-size:0.5em;margin:5px 0 0 0;}
		.messageDetailContent{width:90%;font-size:1.1em;}
		</style>
		<script type="text/javascript"><![CDATA[ 
			function closeMessage(){
				var parent = Util.getWindowById ("pushListWindow");
				parent.document.getElementById("closeMessageBtn").click();
				close();
			}
		]]></script>
	</head>
	<header class="header">
		<div class="header-left">
			<img src="res:image/head_back.png" clicksrc="res:image/head_back_c.png" discache="false" href="javascript:closeMessage()" keybind="back"/>
		</div>
		<div class="header-center">
			<font class="titleFont">推送信息</font>
		</div>
		<div class="header-right">
	
		</div>
	</header>
	<body>
		<br/>
		<div class="round_top"></div>
		<div class="round_middle">
			<div class="messageDetailTitle"><![CDATA[${title}]]></div>
			<div class="messageForm">${timestamp}  来源：<![CDATA[${publisher}]]></div>
			<br/>
			<img src="res:image/jgx.png" style="width:90%;"/>
			<br/>
			<div class="messageDetailContent">
				<![CDATA[${content}]]>
			</div>
		</div>
		<div class="round_bottom"></div>
		<br/>
	</body>
	<menubar show="false" />
</html>
