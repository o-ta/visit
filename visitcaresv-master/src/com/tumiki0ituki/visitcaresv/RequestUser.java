package com.tumiki0ituki.visitcaresv;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * /api/v1/request/user.jsp　が呼ばれたときにこのクラスが呼ばれます.
 * クラス内ではRequestAPIを呼びます。
 *
 * @author o-ta.
 *
 */
@SuppressWarnings("serial")
public class RequestUser extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		new RequestAPI("user", req, resp);
	}

}
