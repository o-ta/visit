package com.tumiki0ituki.visitcaresv;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 * The Class InsertData.
 */
@SuppressWarnings("serial")
public class InsertData extends HttpServlet {

	/** The Constant DB_NAME. */
	private static final String DB_NAME = "jdbc:google:rdbms://GAEサーバーの名前/kaigo";

	/** The c. */
	private Connection mConnect = null;
	
	/** 最小と最大のユーザIDがデータベースから返らない場合にERR_UIDが変える.  */
	private static final int ERR_UID = -1;

	/* (非 Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setCharacterEncoding("UTF-8");
		resp.setContentType("text/html");

		PrintWriter out = resp.getWriter();

		// ドライバマネージャー作成
		try {
			DriverManager.registerDriver(new AppEngineDriver());
			mConnect = DriverManager.getConnection(DB_NAME);

			// 追加するstaff_idを取得
			String staff_id = req.getParameter("staff_id");

			// 日付を取得
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("JST"));
			 
			//user_idの最大値と最小値を取得
			int min_uid = getMinUserId(mConnect);
			int max_uid = getMaxUserId(mConnect);
			Random random = new Random();

			//７時から１６時まで１時間おきにランダムデータを追加
			for (int hour = 7; hour <= 16; hour++) {
				//時間を一時間ずつ増やす
				String timestamp = "2012-" + (cal.get(Calendar.MONTH) + 1)
									+ "-" 
									+ cal.get(Calendar.DAY_OF_MONTH) + " " + hour + ":00:00";
				//存在するuser_idのランダムを生成
				int rnd_uid = random.nextInt(max_uid - min_uid) + min_uid + 1;
				//クエリ
				String sql = "INSERT INTO work (staff_id, user_id,schedule_time) "
								+ "value"
								+ " ('" + staff_id + "','" + rnd_uid + "','" + timestamp + "')";
				
				int success = mConnect.createStatement().executeUpdate(sql);
				if (success > 0) {
					out.println(sql + "<br />");
				} else {
					out.println("処理失敗<br />");
					out.println("<span style=\"color:red;\">" + sql + "<span><br />");
				}
			}

		} catch (SQLException e) {
			out.println("SQLException e<br />");
			out.println(e.toString() + "<br />");
		}

	}

	/**
	 * c_userテーブルのuser_id最大値を取得.
	 *
	 * @param c the c
	 * @return the max user id
	 * @throws SQLException the sQL exception
	 */
	private int getMaxUserId(Connection c) throws SQLException {

		String sql = "SELECT user_id FROM c_user ORDER BY user_id DESC LIMIT 1";
		ResultSet result = c.createStatement().executeQuery(sql);
		if (result.next()) { 
			return result.getInt("user_id");
		}
		return ERR_UID;
	}
	
	/**
	 * c_userテーブルのuser_id最小値を取得.
	 *
	 * @param c the c
	 * @return the min user id
	 * @throws SQLException the sQL exception
	 */
	private int getMinUserId(Connection c) throws SQLException {

		String sql = "SELECT user_id FROM c_user ORDER BY user_id ASC LIMIT 1";
		ResultSet result = c.createStatement().executeQuery(sql);
		if (result.next()) { 
			return result.getInt("user_id");
		}
		return ERR_UID;

	}

}
