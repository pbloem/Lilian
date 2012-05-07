<table>
  <tr>
	<#list names as name>
		<th>${name}</th>
	</#list>
  </tr>
  <#list pairs as pair>
	<tr>
		<#list pair.parameters as parameter>
			<td>${parameter}</td>
		</#list>
		<td>${pair.value}</td>
	</tr>  
  </#list>
</table>

<table>	
  <tr>
	<th>
		mean
	</th>
	<td>
		${mean}
	</td>
  </tr><tr>
	<th>
		(sample) standard deviation
	</th>
	<td>
		${std_dev}
	</td>
  </tr><tr>
	<th>
		median
	</th>
	<td>
		${median}
	</td>
  </tr><tr>
	<th>
		mode
	</th>
	<td>
		${mode}
	</td>
  </tr><tr>	
	<th>
		NaN/infinity values (not counted in the means, stddev etc)
	</th>
	<td>
		${nans}/${infs}
	</td>		
  </tr>	
</table>