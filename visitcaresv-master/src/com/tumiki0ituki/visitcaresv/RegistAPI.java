package com.tumiki0ituki.visitcaresv;

/*
 * タイトル：メイン画面
 * 説明    ：スケジュールリストの一覧を表示する
 *
 * 作成者  ：パソナ太郎
 *
 * 変更履歴
 *        ：新規登録
 *        ：2003.11.01 Saturday
 *        ：レビュー後の修正を反映
 *
 */

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 * /api/v1/regist/以下のAPI部分です。. コンストラクタに必要なデータを渡すだけで、json形式にしてレスポンスを返します。
 * 
 * @author user
 */
public class RegistAPI {

	/** The Constant DB_NAME. */
	private static final String DB_NAME = "jdbc:google:rdbms://GAEサーバーの名前/kaigo";

	/**
	 * Instantiates a new regist api.
	 * 
	 * @param type
	 *            the type　/api/v1/regist/****.jsp　の部分です。
	 * @param req
	 *            the req
	 * @param resp
	 *            the resp
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public RegistAPI(String type, HttpServletRequest req,
			HttpServletResponse resp) throws IOException {

		// Jsonデータとして書きだす宣言 httpheader部分
		resp.setContentType("application/json; charset=UTF-8");
		// http出力をoutと定義する
		PrintWriter out = resp.getWriter();
		// データベースのコネクション
		Connection c = null;
		// データベースに発行するクエリ
		String sql = null;

		try {
			//データベースドライバーを生成
			DriverManager.registerDriver(new AppEngineDriver());
			c = DriverManager.getConnection(DB_NAME);

			// リクエストからsqlを生成
			if (type.equals("location")) {
				sql = locationRegistSQL(req);
			} else if (type.equals("schedule")) {
				sql = scheduleRegist2SQL(req);
			} else {
				// 500 SC_INTERNAL_SERVER_ERROR リクエストの完了を妨げるような HTTP サーバの内部エラー
				resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}

			// クエリ送信　データ登録
			int success = c.createStatement().executeUpdate(sql);
			if (success > 0) {
				//成功した httprequestheader 200を返す
				resp.setStatus(HttpServletResponse.SC_OK);
				out.println("処理成功");
				out.println(sql);
			} else if (success == 0) {
				//失敗した　httprequestheader 400を返す
				resp.setStatus(HttpServletResponse.SC_ACCEPTED);
				out.println("処理失敗");
				out.println(sql);
			}

		} catch (SQLException e) {
			// BAD_REQUEST 400
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			// SQLの発行がうまくいってない
			out.println("doGet SQLException");
			out.println("description:" + e.toString());

		} catch (RequestException e) {
			// BAD_REQUEST 400
			resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			// リクエストのパラメータが正しくない
			out.println("doGet RequestException　リクエストのパラメータが正しくありません。");

		} finally {

			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					// TODO 自動生成された catch ブロック
					out.println("doGet c.close() SQLException");
				}
			}
		}

	}

	/**
	 * ロケーション登録用のSQLを発行します.
	 * 
	 * @param req
	 *            HttpServletRequest
	 * @return String sql リクエストに対応したsql文を返す
	 * @throws RequestException
	 *             the request exception
	 */
	private String locationRegistSQL(HttpServletRequest req)
			throws RequestException {

		String staff_id = req.getParameter("staff_id");
		String latitude = req.getParameter("latitude");
		String longitude = req.getParameter("longitude");

		// 値チェック
		if (staff_id != null && latitude != null && longitude != null) {

			try {
				// 値チェック
				Integer.valueOf(staff_id);
				Double.valueOf(latitude);
				Double.valueOf(longitude);

				return "UPDATE staff_now " + " SET latitude = " + latitude
						+ ", longitude = " + longitude
						+ ", last_update_time = CURRENT_TIMESTAMP"
						+ " WHERE staff_id = " + staff_id;

			} catch (NumberFormatException e) {
				throw new RequestException();
			}

		}

		throw new RequestException();
	}

	/**
	 * スケジュール登録用のSQLを発行します.
	 * 
	 * @param req
	 *            HttpServletRequest
	 * @return String sql リクエストに対応したsql文を返す
	 * @throws RequestException
	 *             the request exception
	 */
	private String scheduleRegist2SQL(HttpServletRequest req)
			throws RequestException {

		String type = req.getParameter("type");

		// データチェック
		if (type != null) {

			if (type.equals("新規")) { // 新規のデータ追加

				String staff_id = req.getParameter("staff_id");
				String user_id = req.getParameter("user_id");
				String schedule_time = req.getParameter("schedule_time");

				// データチェック
				if (staff_id != null && user_id != null
						&& schedule_time != null) {

					return "INSERT INTO work (staff_id, user_id,schedule_time) "
							+ "value"
							+ " ('"
							+ staff_id
							+ "','"
							+ user_id
							+ "','" + schedule_time + "')";

				}

			} else if (type.equals("日報")) { // 既存のデータに日報を付け加える

				String work_id = req.getParameter("work_id");
				String start_time = req.getParameter("start_time");
				String end_time = req.getParameter("end_time");
				String aim = req.getParameter("aim");
				String bath = req.getParameter("bath");
				String clean = req.getParameter("clean");
				String wash = req.getParameter("wash");
				String shopping = req.getParameter("shopping");
				String cook = req.getParameter("cook");
				String wear = req.getParameter("wear");
				String walk = req.getParameter("walk");
				String move = req.getParameter("move");
				String talk = req.getParameter("talk");
				String eat = req.getParameter("eat");
				String sleep = req.getParameter("sleep");
				String note = req.getParameter("note");

				// 値チェック
				if (work_id != null
						&& start_time != null
						&& end_time != null
						&& aim != null
						&& bath != null
						&& clean != null
						&& wash != null
						&& shopping != null
						&& cook != null
						&& wear != null
						&& move != null
						&& talk != null
						&& eat != null
						&& sleep != null
						&& (bath.equals("true") || bath.equals("TRUE")
								|| bath.equals("false") || bath.equals("FALSE"))
						&& (clean.equals("true") || clean.equals("TRUE")
								|| clean.equals("false") || clean
									.equals("FALSE"))
						&& (wash.equals("true") || wash.equals("TRUE")
								|| wash.equals("false") || wash.equals("FALSE"))
						&& (shopping.equals("true") || shopping.equals("TRUE")
								|| shopping.equals("false") || shopping
									.equals("FALSE"))
						&& (cook.equals("true") || cook.equals("TRUE")
								|| cook.equals("false") || cook.equals("FALSE"))
						&& (wear.equals("true") || wear.equals("TRUE")
								|| wear.equals("false") || wear.equals("FALSE"))) {

					if (note == null) {
						note = "なし";
					}

					// DateFormat設定
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					sdf.setLenient(false);

					try {
						// int型チェック
						Integer.valueOf(work_id);
						Integer.valueOf(walk);
						Integer.valueOf(move);
						Integer.valueOf(talk);
						Integer.valueOf(eat);
						Integer.valueOf(sleep);
						// timestamp型チェック
						sdf.parse(start_time);
						sdf.parse(end_time);
					} catch (NumberFormatException e) {
						throw new RequestException();
					} catch (ParseException e) {
						throw new RequestException();
					}

					return "UPDATE work SET " + " start_time = '" + start_time
							+ "'" + " ,end_time = '" + end_time + "'"
							+ " ,status = 1, aim = '" + aim + "'" + " ,bath = " + bath
							+ " ,clean = " + clean + " ,wash = " + wash
							+ " ,shopping = " + shopping + " ,cook = " + cook
							+ " ,wear = " + wear + " ,walk = " + walk
							+ " ,move = " + move + " ,talk = " + talk
							+ " ,eat = " + eat + " ,sleep = " + sleep
							+ " ,note = '" + note + "'" + " WHERE work_id = "
							+ work_id;

				}

			}

		}

		throw new RequestException();
	}
}
