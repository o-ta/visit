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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 * <pre>
 * 【実績訪問データ】 /js/schedule.jsp.
 * 
 * [rest] date 今日の日付 yyyy-mm-dd
 * 
 * [response] 
 * work_id .
 * staff_name スタッフ名 スタッフ名  String .
 * user_name 被介護者名 被介護者の名前 String.
 * schedule_time 予定時間 訪問予定時間 h:m .
 * start_time 開始時間 実際の訪問時刻 h:m .
 * end_time 終了時間 終了時間 h:m 
 * aim 区分 接触時の区分 String 
 * walk 歩行 int 不良・通常・良 不良(2)・通常(1)・良(0)指定なし(-1) 
 * talk 会話 int 不良・通常・良 不良(2)・通常(1)・良(0) 指定なし(-1) 
 * move 移動 int 不良・通常・良 不良(2)・通常(1)・良(0) 指定なし(-1) 
 * sleep 睡眠 int 不良・通常・良 不良(2)・通常(1)・良(0) 指定なし(-1) 
 * eat 食事 int 不良・通常・良 不良(2)・通常(1)・良(0) 指定なし(-1) 
 * imagefix 写真  boolean　imagefix(0or1)で判別し表示 
 * status 日報 boolean　imagefix(0or1)で判別し表示
 * url 画像 画像のURL String
 * 
 * @author o-ta
 * </pre>
 */
@SuppressWarnings("serial")
public class AdminScheduleAPI extends HttpServlet {

	/**
	 * work_idに紐付けられたreportを取得する.
	 * 
	 * @param date
	 *            業務ID.
	 * @return 実行可能SQL
	 */
	private String getScheduleSQL(String date_str) {

		if (date_str != null) {
			String sql = "SELECT " + "work.work_id," + "staff.staff_name, "
					+ "c_user.user_name, "
					+ "DATE_FORMAT(work.schedule_time,'%H:%i') AS schedule_time, "
					+ "DATE_FORMAT(work.start_time, '%H:%i') AS start_time, "
					+ "DATE_FORMAT(work.end_time,'%H:%i') AS end_time, "
					+ "work.aim, " + "work.walk, " + "work.talk, "
					+ "work.move, " + "work.sleep, " + "work.eat, "
					+ "imagefix, " + "status, "
					+ "img.url "
					+ "FROM work LEFT JOIN staff "
					+ "ON work.staff_id = staff.staff_id "
					+ "LEFT JOIN c_user " + "ON work.user_id = c_user.user_id "
					+ "LEFT JOIN (SELECT work_id , url FROM image GROUP BY work_id) AS img " + "ON work.work_id = img.work_id "
					+ "WHERE schedule_time BETWEEN '"
					+ date_str
					+ " 00:00:00' AND '"
					+ date_str
					+ " 23:59:59'"
					+ " ORDER BY schedule_time";
			return sql;
		}
		return null;
	}

	/**
	 * データベース接続のためのURI.
	 */
	private static final String DB_NAME = "jdbc:google:rdbms://GAEサーバーの名前/kaigo";

	/**
	 * dateのフォーマット.
	 */
	private static final String DATE_PATTERN = "yyyy-MM-dd";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		// 出力をJSON指定　httpheaderに書き出し
		resp.setContentType("application/json; charset=UTF-8");
		// 出力をアウトとする
		PrintWriter out = resp.getWriter();
		// エラーメッセージ
		String error_message = null;
		// データベースのコネクション
		Connection c = null;
		// SQL文
		String sql = null;
		// Jsonが格納される予定の配列
		JSONArray json = null;

		// 送られてくるdate
		String date_str = req.getParameter("date");

		// work_idに値があるかどうか？
		if (date_str == null) { // 値がない

			error_message = error_message + "値がないよ！<br/>";

		} else { // 値がある

			try {

				// データベースドライバーを生成
				DriverManager.registerDriver(new AppEngineDriver());
				c = DriverManager.getConnection(DB_NAME);

				checkDate(date_str, DATE_PATTERN);
				// work_idからSQLを発行
				sql = getScheduleSQL(date_str);

				// クエリ実行
				ResultSet result = c.createStatement().executeQuery(sql);
				// Jsonに変換
				json = convertResultSetToJSON(result);

			} catch (NumberFormatException e) {

				// 値が数字じゃない
				error_message = error_message + "値が数字じゃないよ！<br />";

			} catch (SQLException e) {

				// SQLエラー
				error_message = error_message + "SQLエラーだよ！<br />内容："
						+ e.toString() + "<br />";

			} catch (ParseException e) {

				// 与えられたパラメータが正しくDateに変換出来なかった
				// SQLエラー
				error_message = error_message + "Dateformatエラーだよ！<br />"
						+ "date:" + date_str + "<br />" + "内容：" + e.toString()
						+ "<br />";

			} finally {

				if (c != null) { // データベース接続終了
					try {
						c.close();
					} catch (SQLException e) {
						error_message = error_message
								+ "データベースが正常に終了しなかったよ<br/>";
					}
				}
				if (json != null) { // jsonが正常に生成されていれば出力
					out.println(json.toString());
				} else { // エラー
					out.println("json is null<br />");
					out.println(error_message);
				}
			}
		}

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
	 * ResultSet. to. JSON. ResultSetをJSON形式に変換します。
	 * 
	 * RequestAPI.javaで書いたやつを流用 クラスにしろよ！ヾ(・ε・。)
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
