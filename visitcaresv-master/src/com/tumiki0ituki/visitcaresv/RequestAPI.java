package com.tumiki0ituki.visitcaresv;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 * /api/v1/request/以下の処理をすべてこちらで引き受けています。.
 * 
 * @author o-ta.
 * 
 */
public class RequestAPI {

	/**
	 * データベース接続のためのURI.
	 */
	private static final String DB_NAME = "jdbc:google:rdbms://GAEサーバーの名前/kaigo";
	
	/**
	 * dateのフォーマット.
	 */
	private static final String DATE_PATTERN = "yyyy-MM-dd";
	
	/**
	 * コンストラクタです。　欲しいリクエストのタイプを指定して、Httpのrequestとresponseを渡してください。.
	 * json形式にして吐き出します。.
	 * 
	 * @param type
	 *            次のURLの*部分です.　/api/v1/****.jsp
	 * @param req
	 *            doGet doPostで渡される HttpServletRequest.
	 * @param resp
	 *            doGet doPostで渡される HttpServletResponse.
	 * @throws IOException
	 *             HttpServletResponse が吐き出す
	 */
	public RequestAPI(String type, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {

		// Jsonデータとして書きだす宣言 httpheader部分
		resp.setContentType("application/json; charset=UTF-8");
		// http出力をoutと定義する
		PrintWriter out = resp.getWriter();

		// Jsonが格納される予定の配列
		JSONArray json = null;
		// データベースのコネクション
		Connection c = null;
		// データベースに発行するクエリ
		String sql = null;
		try {
			// データベースドライバーを生成
			DriverManager.registerDriver(new AppEngineDriver());
			c = DriverManager.getConnection(DB_NAME);

			// リクエストからsqlを生成
			if (type.equals("schedule")) { // schedule.jsp
				sql = scheduleReq2SQL(req);
			} else if (type.equals("staff")) { // staff.jsp
				sql = staffReq2SQL(req);
			} else if (type.equals("user")) { // user.jsp
				sql = userReq2SQL(req);
			} else {
				// 500 SC_INTERNAL_SERVER_ERROR リクエストの完了を妨げるような HTTP サーバの内部エラー
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
			// クエリ実行
			ResultSet result = c.createStatement().executeQuery(sql);
			// Jsonに変換
			json = convertResultSetToJSON(result);

		} catch (RequestException e) {
			// リクエストのパラメータが正しくない
			out.println("doGet RequestException　リクエストのパラメータが正しくありません。");
		} catch (SQLException e) {
			// SQLの発行がうまくいってない
			out.println("doGet SQLException");
			out.println("description:" + e.toString());
			out.println("SQL:" + sql);
		} finally {
			if (c != null) { // データベース接続終了
				try {
					c.close();
				} catch (SQLException e) {
					out.println("doGet c.close() SQLException");
				}
			}
			if (json != null) { // jsonが正常に生成されていれば出力
				out.println(json.toString());
			} else { // エラー
				out.println("json is null");
			}

		}
	}

	/**
	 * スケジュールのリクエストから適切なsql文を発行します。 リクエストが不正の場合はnullを返します。.
	 * 
	 * @param req
	 *            HttpServletRequest
	 * @return String sql リクエストに対応したsql文を返す
	 * @throws RequestException
	 *             リクエストのパラメータが正しくない場合に吐き出されます。
	 */
	private String scheduleReq2SQL(HttpServletRequest req)
			throws RequestException {

		String staff_id = req.getParameter("staff_id");
		String date_of = req.getParameter("date_of");
		try {
			checkDate(date_of, DATE_PATTERN);
		} catch (ParseException e) {
			//文字列の日付の型がおかしい
			throw new RequestException();
		}

		if (staff_id != null && date_of != null) {
			String sql = "SELECT (@i:=@i+1) as turn, work_id, work.user_id, user_name, latitude, longitude, "
					+ " DATE_FORMAT(schedule_time,'%H:%i') AS schedule_time, CONCAT(city,block) AS address , status,imagefix"
					+ " FROM (select @i:=0) as dummy,work LEFT JOIN c_user "
					+ " ON work.user_id = c_user.user_id "
					+ " WHERE work.staff_id = "
					+ staff_id
					+ " AND schedule_time  BETWEEN '"
					+ date_of
					+ " 00:00:00' AND '"
					+ date_of
					+ " 23:59:59'"
					+ " ORDER BY schedule_time";
			return sql;
		}

		throw new RequestException();

	}
	
	/**
	 * 文字列の日付が正しいかどうか・.
	 * @param date_str 文字列の日付.
	 * @param pattern　パターン.
	 * @throws ParseException エラー
	 */
	private void checkDate(String date_str, String pattern)
			throws ParseException {

		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		sdf.parse(date_str);

	}

	/**
	 * 被介護者のリクエストから適切なsql文を発行します。 リクエストが不正の場合はnullを返します。.
	 * 
	 * @param req
	 *            HttpServletRequest
	 * @return String sql リクエストに対応したsql文を返す
	 * @throws RequestException
	 *             リクエストのパラメータが正しくない場合に吐き出されます。
	 */
	private String userReq2SQL(HttpServletRequest req) throws RequestException {

		return "SELECT user_id , user_name FROM c_user";
	}

	/**
	 * スタッフのリクエストから適切なsql文を発行します。 リクエストが不正の場合はnullを返します。.
	 * 
	 * @param req
	 *            HttpServletRequest
	 * @return String sql リクエストに対応したsql文を返す
	 * @throws RequestException
	 *             リクエストのパラメータが正しくない場合に吐き出されます。
	 */
	private String staffReq2SQL(HttpServletRequest req) throws RequestException {

		String staff_id = req.getParameter("staff_id");
		String location = req.getParameter("location");

		// 全員分のデータが必要
		if (Integer.valueOf(staff_id) < 0 && staff_id != null
				&& location != null) {

			if (location.equals("TRUE") || location.equals("true")) { // ロケーションが必要

				return "SELECT "
						+ "staff_now.staff_id, "
						+ "staff_now.latitude, "
						+ "staff_now.longitude, "
						+ "DATE_FORMAT(staff_now.last_update_time, '%Y-%m-%d %H:%i') AS last_update_time, "
						+ "staff.staff_name FROM staff_now LEFT JOIN staff "
						+ "ON staff_now.staff_id = staff.staff_id "
						+ "ORDER BY staff_id";

			} else if (location.equals("FALSE") || location.equals("false")) { // ロケーションが不要

				return "SELECT staff_now.staff_id , staff.staff_name "
						+ "FROM staff_now LEFT JOIN staff "
						+ "ON staff_now.staff_id = staff.staff_id "
						+ "ORDER BY staff_id";
			}

			// 特定のスタッフのデータが必要
		} else if (staff_id != null && Integer.valueOf(staff_id) >= 0
				&& location != null) {

			if (location.equals("TRUE") || location.equals("true")) { // ロケーションが必要

				return "SELECT "
						+ "staff_now.staff_id, "
						+ "staff_now.latitude, "
						+ "staff_now.longitude, "
						+ "DATE_FORMAT(staff_now.last_update_time, '%Y-%m-%d %H:%i') AS last_update_time, "
						+ "staff.staff_name FROM staff_now LEFT JOIN staff "
						+ "ON staff_now.staff_id = staff.staff_id "
						+ "WHERE staff_now.staff_id = " + staff_id + " "
						+ "ORDER BY staff_id";

			} else if (location.equals("FALSE") || location.equals("false")) { // ロケーションが不要

				return "SELECT staff_now.staff_id , staff.staff_name "
						+ "FROM staff_now LEFT JOIN staff "
						+ "ON staff_now.staff_id = staff.staff_id "
						+ "WHERE staff_now.staff_id = " + staff_id + " "
						+ "ORDER BY staff_id";
			}

		}
		// パラメータに問題あり
		throw new RequestException();

	}

	/**
	 * ResultSet. to. JSON. ResultSetをJSON形式に変換します。
	 * 
	 * @param rs
	 *            ResultSet
	 * @return JSONArray
	 */
	private JSONArray convertResultSetToJSON(java.sql.ResultSet rs) {

		JSONArray json = new JSONArray();

		try {

			java.sql.ResultSetMetaData rsmd = rs.getMetaData();

			while (rs.next()) {
				int numColumns = rsmd.getColumnCount();
				// Jsonオブジェクト
				JSONObject obj = new JSONObject();

				for (int i = 1; i < numColumns + 1; i++) {

					String column_name = rsmd.getColumnName(i);

					// データの型をチェック
					if (rsmd.getColumnType(i) == java.sql.Types.BOOLEAN) {
						obj.put(column_name, rs.getBoolean(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.DOUBLE) {
						obj.put(column_name, rs.getDouble(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.FLOAT) {
						obj.put(column_name, rs.getFloat(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.INTEGER) {
						obj.put(column_name, rs.getInt(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.NVARCHAR) {
						obj.put(column_name, rs.getNString(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.VARCHAR) {
						obj.put(column_name, rs.getString(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.TINYINT) {
						obj.put(column_name, rs.getInt(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.SMALLINT) {
						obj.put(column_name, rs.getInt(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.DATE) {
						obj.put(column_name, rs.getDate(column_name));
					} else if (rsmd.getColumnType(i) == java.sql.Types.TIMESTAMP) {
						obj.put(column_name, rs.getTimestamp(column_name));
					} else {
						obj.put(column_name, rs.getObject(column_name));
					}

				} // end foreach
				json.put(obj);

			} // end while

		} catch (SQLException e) {

			e.printStackTrace();
		} catch (JSONException e) {

			e.printStackTrace();
		}

		return json;
	}

}
