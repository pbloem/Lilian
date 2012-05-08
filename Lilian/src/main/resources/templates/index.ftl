<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>

  <link type="text/css" rel="stylesheet" media="all" href="./css/common.lilian.css" />
  <link type="text/css" rel="stylesheet" media="screen" href="./css/screen.lilian.css" />
  
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js" type="text/javascript"></script>
  <script src="http://d3js.org/d3.v2.js" type="text/javascript"></script>  
  
  <title>Lilian results</title>
</head>
<body>
  <h1>Report: ${short_name}</h1>
  <h2>Run information</h2>
  <p class="explanation">
  	Basic information about this run of the experiment.
  </p>
  
  <dl>
  	<dt>
  		Experiment name
  	</dt>
  	<dd>
  		${name}
  	</dd>
  	
    <dt>
  		description
  	</dt>
  	<dd>
  		${description}
  	</dd>
  	<dt>
  		Start date/time
  	</dt>  	
  	<dd>
  		${start_date_time} (${start_millis})
  	</dd>
  	<dt>
  		End date/time
  	</dt>  	
  	<dd>
  		${end_date_time} (${end_millis})
  	</dd>
  </dl>
  
  <h2>Results</h2>
  <p class="explanation">
    The relevant results of the run, collated and analysed.
  </p>
  
  <#list results as result>
    <h3>Result: ${result.name}</h3>
    <p class="description">
    	${result.description}
    </p>
    <p class="value">
    	${result.value}
    </p>
  </#list>
  
  <h2>Reportables</h2>
   <p class="explanation">
	Extensive information about the state of all software in the classpath at 
	the time of the run
  </p> 
  
</body>
</html>  