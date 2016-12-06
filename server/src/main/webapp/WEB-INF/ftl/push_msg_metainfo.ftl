<metainfo>
	<doc type="form_xmap/form_gaea">
		<title>${sys.title?xml}</title>
		<id>$DOCID</id>
		<updated></updated>
		<forminfo>
			<appid>imobii@fiberhome</appid>
			<url>${sys.url?xml}</url>
			<cookies></cookies>
			<method>POST</method>
			<charset>UTF-8</charset>
			<params>
				<param name="userCode">$USERNAME</param>
				<param name="PASSWORD">$PASSWORD</param>
				<param name="tenantCode">$COMPANY</param>
				<param name="gaea_push">${sys.sign?xml}</param>
				<#list user?keys as prop>
				<param name="${prop?xml}">${user[prop]?xml}</param>
				</#list>
			</params>
			<expire></expire>
		</forminfo>
	</doc>
</metainfo>
