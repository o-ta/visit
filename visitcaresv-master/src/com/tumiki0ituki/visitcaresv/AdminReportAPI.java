package com.tumiki0ituki.visitcaresv;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 *
 * 【日報データ】
 *  /js/report.jsp
 *
 * [rest]
 * work_id 業務番号		業務ID typeがreportの場合に必要
 *
 * [response](JSON)
 * staff_name スタッフ名	スタッフ名 String
 * user_name 被介護者名	被介護者の名前 String
 * start_time 開始時間	実際の訪問時刻 YYYY-MM-DD h:m
 * end_time 終了時間		実際の終了時刻 h:m
 * aim 区分				接触時の区分 String
 * bath 入浴				boolean／チェックボックス
 * clean 掃除				boolean／チェックボックス
 * wash 洗濯				boolean／チェックボックス
 * shopping 買い物		boolean／チェックボックス
 * cook 一般料理			boolean／チェックボックス
 * wear 衣服整理			boolean／チェックボックス
 * walk 歩行				int 不良・通常・良  不良(2)・通常(1)・良(0) 指定なし(-1)
 * move 移動				int 不良・通常・良  不良(2)・通常(1)・良(0) 指定なし(-1)
 * talk 会話				int 不良・通常・良  不良(2)・通常(1)・良(0) 指定なし(-1)
 * eat 食事				int 不良・通常・良  不良(2)・通常(1)・良(0) 指定なし(-1)
 * sleep 睡眠				int 不良・通常・良  不良(2)・通常(1)・良(0) 指定なし(-1)
 * note 内容				備考欄 String
 * url 画像				画像のURL String
 *
 * @author o-ta.
 *
 */
@SuppressWarnings("serial")
public class AdminReportAPI extends HttpServlet {

	/**
	 * データベース接続のためのURI.
	 */
	private static final String DB_NAME = "jdbc:google:rdbms://GAEサーバーの名前/kaigo";

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

		// 送られてくるwork_id
		String work_id_str = req.getParameter("work_id");
		// work_idに値があるかどうか？
		if (work_id_str == null) { // 値がない

			error_message = error_message + "値がない！<br/>";

		} else { // 値がある

			try {

				// データベースドライバーを生成
				DriverManager.registerDriver(new AppEngineDriver());
				c = DriverManager.getConnection(DB_NAME);

				int work_id = Integer.valueOf(work_id_str);
				// work_idからSQLを発行
				sql = getReportSQL(work_id);

				// クエリ実行
				ResultSet result = c.createStatement().executeQuery(sql);
				// Jsonに変換
				json = convertResultSetToJSON(result);

			} catch (NumberFormatException e) {

				// 値が数字じゃない
				error_message = error_message + "値が数字じゃない！<br/>";

			} catch (SQLException e) {

				// SQLエラー
				error_message = error_message + "SQLエラー！<br/>内容："
						+ e.toString() + "<br/>";

			} finally {

				if (c != null) { // データベース接続終了
					try {
						c.close();
					} catch (SQLException e) {
						error_message = error_message + "データベースが正常に終了しなかった<br/>";
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
	 * work_idに紐付けられたreportを取得する.
	 *
	 * @param work_id 業務ID.
	 * @return
	 */
	private String getReportSQL(int work_id) {

		if (work_id >= 0) {
			String sql = "SELECT "
					+ "staff.staff_name, "
					+ "c_user.user_name, "
					+ "DATE_FORMAT(work.start_time, '%Y-%m-%d %T') AS start_time, "
					+ "DATE_FORMAT(work.end_time,'%T') AS end_time, "
					+ "work.aim, "
					+ "CASE WHEN work.bath = 1 THEN 'true' ELSE 'false' END AS bath, "
					+ "CASE WHEN work.clean = 1 THEN 'true' ELSE 'false' END AS clean, "
					+ "CASE WHEN work.wash = 1 THEN 'true' ELSE 'false' END AS wash, "
					+ "CASE WHEN work.shopping = 1 THEN 'true' ELSE 'false' END AS shopping, "
					+ "CASE WHEN work.cook = 1 THEN 'true' ELSE 'false' END AS cook, "
					+ "CASE WHEN work.wear = 1 THEN 'true' ELSE 'false' END AS wear, "
					+ "work.walk, " + "work.move, " + "work.talk, "
					+ "work.eat, " + "work.sleep, " + "work.note, "
					+ "image.url " + "FROM work LEFT JOIN staff "
					+ "ON work.staff_id = staff.staff_id "
					+ "LEFT JOIN c_user " + "ON work.user_id = c_user.user_id "
					+ "LEFT JOIN image " + "ON work.work_id = image.work_id "
					+ "WHERE work.work_id = " + work_id + " " + "LIMIT 1";
			return sql;
		}
		return null;
	}

	/**
	 * ResultSet. to. JSON. ResultSetをJSON形式に変換します.
	 *
	 * RequestAPI.javaで書いたやつを流用 クラスに…
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
