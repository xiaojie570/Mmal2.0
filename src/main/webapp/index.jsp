<%@ page language="java"  contentType="text/html; charset=UTF-8" %>
<html>
<body>
<h2>tomcat12222222!</h2>
<h2>tomcat1111!</h2>
<h2>tomcat1111!</h2>
<h2>Hello World!</h2>

springmvc上传文件
<form name="form1" action="/Mmall/manage/product/upload.do" method="post" enctype="multipart/form-data">
    <input type = "file" name="upload_file"> <!-- 这个name一定要与MultipartFile的对象名字一样 -->
    <input type="submit" value="springmvc上传文件">
</form>



富文本图片上传文件
<form name="form2" action="/Mmall/manage/product/richtext_img_upload.do" method="post" enctype="multipart/form-data">
    <input type="file" name="upload_file" />
    <input type="submit" value="富文本图片上传文件" />
</form>
</body>
</html>
