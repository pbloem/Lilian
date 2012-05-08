<table>
  <tr>
	<#list row_headers as header>
		<th>${header}</th>
	</#list>
  </tr>
  <#list rows as row>
	<tr>
		<#list row.parameters as parameter>
			<td class="parameter">${parameter}</td>
		</#list>
		<#list row.values as value>
			<td class="value">${value}</td>
		</#list>
	</tr>  
  </#list>
</table>