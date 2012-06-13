<div class="list-section js-tabs">
	
	<ul>
		<li><a href="#tabs-1">Summary</a></li>
		<li><a href="#tabs-2">Full data (raw)</a></li>
	</ul>		
			
	<div id="tabs-1">
		<h4>Tabular data</h4>
	
		<table>
				<tr>
					<th>height</th><td>${height}</td>
				</tr>
				<tr>
					<th>width</th><td>${width}</td>
				</tr>
		</table>
	</div>
		
	<div id="tabs-2">
		<table class="raw-data raw-data-${id}">
		
		<#list table as row>
			<tr>
			<#list row as cell>
			  <td>${cell}</td>
			</#list>
			</tr>
		</#list>
		</table>
	</div>
	
</div>