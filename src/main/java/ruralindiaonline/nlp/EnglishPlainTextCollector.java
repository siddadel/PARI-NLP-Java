package ruralindiaonline.nlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.razorpay.RazorpayException;

public class EnglishPlainTextCollector {

	public static void main(String[] args) throws RazorpayException, SQLException, IOException {
		Properties prop = new Properties();
		prop.load(App.class.getResourceAsStream("/application.properties"));
		posgres(prop);

	}

	public static void posgres(Properties prop) throws SQLException, IOException {
		ResultSet rs = DriverManager
				.getConnection("jdbc:postgresql://" + prop.getProperty("url") + "/" + prop.getProperty("db"),
						prop.getProperty("username"), prop.getProperty("password"))
				.createStatement().executeQuery(prop.getProperty("query_dictionary"));

		while (rs.next()) {
			StringBuilder sb = new StringBuilder();
			JSONArray jsonArray = new JSONArray(rs.getString("content"));
			String language = rs.getString("language");
			String slug = rs.getString("slug");
			String title = rs.getString("title");
			String strap = rs.getString("strap");

			Iterator<Object> i = jsonArray.iterator();

			while (i.hasNext()) {

				JSONObject object = (JSONObject) i.next();

				String type = object.getString("type");
				JSONObject value = (JSONObject) object.get("value");

				String content = null;
				switch (type) {
				case "paragraph":
					content = (value.get("content").toString());
					break;
				case "image_with_quote_and_paragraph":
					content = (((JSONObject) value.get("quote")).get("quote").toString());
					break;
				case "columnar_image_with_text":
					content = (((JSONObject) value.get("content")).get("content").toString());
					break;
				case "video_with_quote":
					content = (value.get("quote").toString());
					break;
				case "paragraph_with_page":
					content = (((JSONObject) value.get("content")).get("content").toString());
					break;
				case "full_width_image":
					content = (value.get("caption").toString());
					break;
				}

				if (content != null) {
					sb.append(clean(content)).append("\n");
				}
			}

			write("D:\\pari_new3\\" + slug+".txt", sb.toString());
		}
	}

	private static void write(String fileName, String text) throws IOException {
		Writer fstream = null;
		BufferedWriter out = null;
		fstream = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8);
		out = new BufferedWriter(fstream);
		out.write(text);
		out.newLine();

		out.flush();
		out.close();
	}

	private static String clean(String text) {
		return text.replaceAll("<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});", " ").replaceAll("\n", " ")
				.replaceAll(" {2,}", " ").trim();
	}
}
