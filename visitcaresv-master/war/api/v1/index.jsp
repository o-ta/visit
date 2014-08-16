<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>TEST</title>
<style type="text/css">
	  strong
	  {
	  	font-weight: bold;
	  	color:#666666;
	  	padding:12px;
	  	
	  }
      dt
      {
         font-weight: bold;
         border-bottom: solid 4px #cccccc;
         color: #666666;
         padding-bottom: 2px;
         text-indent:2px;
         margin-top: 1em;
      }
      
      dd
      {
         border: solid 1px #cccccc;
         padding: 1em;
         margin-left: 0em;
         margin-top: 0.5em;
      }
     table.sample
     {
		border-top:1px solid #663300;
		border-left:1px solid #663300;
		border-collapse:collapse;
		border-spacing:0;
		background-color:#ffffff;
		empty-cells:show;
		width:600px;
		margin-top:6px;
	  	margin-bottom:12px;
	}
	.sample td{
	    border-right:1px solid #663300;
	    border-bottom:1px solid #663300;
	    padding:0.3em 1em;
	}
   </style>
<script type="text/javascript">
<!--
window.onbeforeunload = function(){location.reload(true);}
window.onunload = function{}
-->
</script>
</head>
<body>

<%@page import="com.google.appengine.api.blobstore.BlobstoreService" %>
<%@page import="com.google.appengine.api.blobstore.BlobstoreServiceFactory" %>
<%@page import="java.sql.Connection" %>
<%@page import="com.google.appengine.api.rdbms.AppEngineDriver" %>
<%@page import="java.sql.DriverManager" %>
<%@page import="java.sql.ResultSet" %>
<%! String DB_NAME = "jdbc:google:rdbms://GAEサーバーの名前/kaigo"; %>
<%
	BlobstoreService blobStoreService = BlobstoreServiceFactory.getBlobstoreService();
%>
<html>
<head>
</head>
<body>
	<dl>
		<dt>画像登録テスト</dt>
		<dd>
		    <form action="<%= blobStoreService.createUploadUrl("/api/v1/regist/file.jsp") %>" method="post" enctype="multipart/form-data">
		        <input type="file" name="img">
		        <input type="submit" value="Submit">
		    </form>
	    </dd>
	    <dt>テストデータ挿入</dt>
	    <dd>
	    	<form action="insert_data.jsp" method="post">
	    		<select name="staff_id">
<%
//データベース接続
DriverManager.registerDriver(new AppEngineDriver());
Connection c = DriverManager.getConnection(DB_NAME);

//スタッフ取得
String sql = "SELECT staff_id , staff_name FROM staff ORDER BY staff_id";
ResultSet result = c.createStatement().executeQuery(sql);
while(result.next()){
%>
	<option value="<%= result.getInt("staff_id") %>" >ID:<%= result.getInt("staff_id") %> NAME:<%= result.getString("staff_name") %></option>
<%
}
result.close();
c.close();
%>
	    		
	    		</select>
	    		<input type="submit" value="追加" />
	    	</form>
	    </dd>
    </dl>
</body>
</html>
</body>
</html>