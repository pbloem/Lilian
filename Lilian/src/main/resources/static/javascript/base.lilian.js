$(function() {
	$(".js-tabs").tabs();
	
	var time = new Rickshaw.Fixtures.Time();
	var unit = time.unit('seconds');	
	
	data = load($('.raw-data'), ["x", "y"], true);
			
	var graph = new Rickshaw.Graph( {
	    element: document.querySelector(".rs-line .chart"),
	    renderer:  'line',
	    width: 760,
	    height: 100,
	    series: [{
	        color: 'steelblue',
	        data: data
	    }]
	});	
	
	var y_ticks = new Rickshaw.Graph.Axis.Y( {
		graph: graph,
		orientation: 'left',
        tickFormat: Rickshaw.Fixtures.Number.formatKMBT,
        element: document.querySelector(".rs-line .y-axis")
	});
	
	var x_ticks = new Rickshaw.Graph.Axis.Time( {
		graph: graph,
		timeUnit: unit
	});	
	
	graph.render();
	
	histoData = load($('.histogram-data'), ["x", "y"]);
	
	var histo = new Rickshaw.Graph( {
	    element: document.querySelector(".rs-line-histogram .chart"),
	    renderer:  'bar',
	    width: 760,
	    height: 100,
	    series: [{
	        color: 'steelblue',
	        data: histoData
	    }]
	});	
	
	var x_ticks = new Rickshaw.Graph.Axis.Time( {
		graph: histo,
		timeUnit: unit
	});
	
	
	histo.render();
});

/**
 * Loads data from the given html table into an array
 * @param html
 */
function load(htmlTable)
{
	data = [];
	
	headers = [];
	htmlTable.find('tr th').each(function(){
		headers.push($(this).html());
	});
	
	return load(htmlTable, headers);
}

function load(htmlTable, headers)
{
	return load(htmlTable, headers, false);
}

/**
 * Returns the data from the html table using the given headers, assigning the 
 * index of the row to the first header
 * @returns
 */
function load(htmlTable, headers, withIndex)
{
	data = [];
		
	htmlTable.find('tr').each(function(i, value){
		if($(value).find('td').size() > 0)
		{				
			dataRow = {}
			if(withIndex)
				dataRow[headers[0]] = i;
			
			$(this).find('td').each(function(index, value){
				if(withIndex)
				{
					if(index < headers.length + 1)
					{
						dataRow[headers[index+1]] = parseInt($(value).html());
						
					}
				} else 
				{
					if(index < headers.length)
					{
						dataRow[headers[index]] = parseInt($(value).html());
					}
				}
					
			})
			
			data.push(dataRow);
		}
	});
	
	return data;
}