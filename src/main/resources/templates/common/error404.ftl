<#assign base=req.contextPath />
<!DOCTYPE html>
<html>
	<head>
        <base id="base" href="${base}">
		<title>Zookeeper-Web</title>
		<script src="${base}/static/js/jquery.min.js" type="text/javascript"></script>
		<link href="${base}/static/css/bootstrap.min.css" rel="stylesheet" type="text/css">
		<link href="${base}/static/css/zkApi-web.css" rel="stylesheet" type="text/css">
	</head>
	<body>

<div class="container">
    <div class="row">
        <div class="col-md-12">
            <div class="error-template">
                <h1>
                    卧擦!</h1>
                <h2>
                    404 Not Found</h2>
                <div class="error-details">
                    这个页面跑出去瞎逛了,到现在还没回来呢(⊙o⊙)
                </div>
                <div class="error-actions">
                    <a href="${base}" class="btn btn-primary btn-lg"><span class="glyphicon glyphicon-home"></span> 回主页 </a>
                </div>
            </div>
        </div>
    </div>
</div>

	</body>
</html>