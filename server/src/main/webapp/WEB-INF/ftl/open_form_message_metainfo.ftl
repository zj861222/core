<metainfo>
	<doc type="form_xmap/form_gaea">
		<title>${sys.docTitle?xml}</title>
		<id>$DOCID</id>
		<updated>${sys.updated?xml}</updated>
		<forminfo>
			<appid>${sys.appid?xml}</appid>
			<url>${sys.url?xml}</url>
			<cookies>${sys.cookies?xml}</cookies>
			<method>${sys.method?xml}</method>
			<charset>${sys.charset?xml}</charset>
			<params>
				<param name="userCode">$USERNAME</param>
				<param name="PASSWORD">$PASSWORD</param>
				<param name="tenantCode">$COMPANY</param>
				<param name="gaea_push">1</param>
				<#list user?keys as prop>
				<param name="${prop?xml}">${user[prop]?xml}</param>
				</#list>
			</params>
			<expire>${sys.expire?xml}</expire>
		</forminfo>
	</doc>
</metainfo>
