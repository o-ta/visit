package com.tumiki0ituki.visitcaresv;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.rdbms.AppEngineDriver;

/**
 * @author o-ta
 *
 */
@SuppressWarnings("serial")
public class RegistFile extends HttpServlet {

	/**
	 * データベース接続のためのURI.dflkamdsfaklsdjfasndkjn
	 */
	private static final String DB_NAME = "jdbc:google:rdbms://GAEサーバーの名前/kaigo";

	/**
	 * ブロブストアサービス.
	 */
	private BlobstoreService mBlobSV = BlobstoreServiceFactory
			.getBlobstoreService();

	/**
	 * データベース接続.
	 */
	private Connection mConnect = null;

	@SuppressWarnings("deprecation")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		resp.setContentType("text/html");
		resp.setCharacterEncoding("UTF-8");
		PrintWriter out = resp.getWriter();

		Map<String, BlobKey> blobs = mBlobSV.getUploadedBlobs(req);
		BlobKey blobkey = blobs.get("img");

		if (blobkey != null) { // 無事にファイル取得完了
			// ブロブインフォより画像の情報を取得
			BlobInfoFactory factory = new BlobInfoFactory();
			BlobInfo blobinfo = factory.loadBlobInfo(blobkey);
			String filename = getName(blobinfo.getFilename()); // ファイル名取得　拡張子削除

			// イメージサービスより画像のURLを取得
			ImagesService imagesService = ImagesServiceFactory
					.getImagesService();
			String image_url = imagesService.getServingUrl(blobkey);

			// 画像保存クエリ作成
			String sql = "INSERT INTO image (work_id , url) " + "VALUE" + "('"
					+ filename + "','" + image_url + "')";

			// データベースに画像情報登録
			try {
				// 画像の名前が数字かどうか？
				Integer.valueOf(filename);

				DriverManager.registerDriver(new AppEngineDriver());
				mConnect = DriverManager.getConnection(DB_NAME);
				// クエリ送信　データ登録
				int success = mConnect.createStatement().executeUpdate(sql);
				if (success == 1) {

					// データベースに画像登録済みとする
					sql = "UPDATE work SET imagefix = 1 WHERE work_id = "
							+ filename;
					success = mConnect.createStatement().executeUpdate(sql);

					if (success == 1) {
						// 成功！！
						out.println("CONTENT-TYPE:" + blobinfo.getContentType() + "<br />");
						out.println("FILENAME:" + blobinfo.getFilename() + "<br />");
						out.println("FILESIZE:" + blobinfo.getSize() + "<br />");
						out.println("FILE URL:<a href=\"" + image_url + "\" >" + image_url + "<br />");

					} else if (success == 0) {
						out.println("ERROR:データベース書き込みエラー　画像登録済みに変更できませんでした。<br />");
					}

				} else if (success == 0) {
					out.println("ERROR:データベース書き込みエラー　画像URLを登録できませんでした。<br />");
				}

			} catch (SQLException e) {
				out.println("ERROR:データベース処理エラー<br />");
				out.println(e.toString());
				out.println("<br />" + sql + "<br />");
			} catch (NumberFormatException e) {
				out.println("ERROR:ファイル名が数字ではありません。<br />");
				out.println(e.toString());
			}

		} else {
			resp.getWriter().println("ERROR:BlobKeyを取得できませんでした。<br />");
		}

	}

	/**
	 * ファイル名から拡張子を取り除いた名前を返します.
	 * 
	 * @param fileName
	 *            ファイル名
	 * @return ファイル名
	 */
	public static String getName(String fileName) {
		if (fileName == null) {
			return null;
		}

		int point = fileName.lastIndexOf(".");
		if (point != -1) { //.があるか
			return fileName.substring(0, point);
		}
		return fileName;
	}

}
