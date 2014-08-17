package com.tumiki0ituki.visitcaresv;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;


/**
 * /api/v1/request/upload.jsp　が呼ばれたときにこのクラスが呼ばれます.
 * blobストアにアップロードするためのUrlをjson形式にて返します。
 *
 * @author o-ta.
 *
 */
@SuppressWarnings("serial")
public class RequestUpload extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("application/json; charset=UTF-8");
		PrintWriter out = resp.getWriter();

		//Upload用のurlを生成
		BlobstoreService service = BlobstoreServiceFactory.getBlobstoreService();
		String uploadUrl = service.createUploadUrl("/api/v1/regist/file.jsp");

		//json形式にして格納
		String json = "[{\"url\":\"" + uploadUrl +"\"}]";

		out.print(json);

	}

}
