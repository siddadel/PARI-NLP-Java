package ruralindiaonline.nlp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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

public class TranslatedContentDownloader {

	public static void main(String[] args) throws RazorpayException, SQLException, IOException {
		Properties prop = new Properties();
		prop.load(App.class.getResourceAsStream("/application.properties"));
		posgres(prop);

	}

	public static void posgres(Properties prop) throws SQLException, IOException {
		ResultSet rs = DriverManager
				.getConnection("jdbc:postgresql://" + prop.getProperty("url") + "/" + prop.getProperty("db"),
						prop.getProperty("username"), prop.getProperty("password"))
				.createStatement().executeQuery(prop.getProperty("query_content"));
		while (rs.next()) {

			JSONArray jsonArray = new JSONArray(rs.getString("eng_modular_content"));
			JSONArray transArray = new JSONArray(rs.getString("trans_modular_content"));
			String language = rs.getString("trans_language");
			String engSlug = rs.getString("eng_slug");
			String title = rs.getString("eng_title");
			String transTitle = rs.getString("trans_title");
			String strap = rs.getString("eng_strap");
			String transStrap = rs.getString("trans_strap");

			write(title, transTitle, 0, "title", language, engSlug);
			write(strap, transStrap, 0, "strap", language, engSlug);

			Iterator<Object> i = jsonArray.iterator();
			Iterator<Object> j = transArray.iterator();

			int count = 0;
			while (i.hasNext() && j.hasNext()) {

				JSONObject object = (JSONObject) i.next();
				JSONObject trans = (JSONObject) j.next();

				String type = object.getString("type");
				JSONObject value = (JSONObject) object.get("value");

				String transType = trans.getString("type");
				JSONObject transValue = (JSONObject) trans.get("value");

				if (!type.equals(transType)) {
					System.err.println(value);
					System.err.println(transValue);
					System.err.println("_________________________________________");
				} else {
					switch (type) {
					case "paragraph":
						writeValue(value, transValue, "content", count, type, language, engSlug);
						break;
					case "image_with_quote_and_paragraph":
						writeNestedValue(value, transValue, "quote", count, type, language, engSlug);
						break;
					case "columnar_image_with_text":
						writeValue(value, transValue, "caption", count, type, language, engSlug);
						writeNestedValue(value, transValue, "content", count, type, language, engSlug);
						break;
					case "video_with_quote":
						writeValue(value, transValue, "quote", count, type, language, engSlug);
						break;
					case "paragraph_with_page":
						writeNestedValue(value, transValue, "content", count, type, language, engSlug);
						break;
					case "full_width_image":
						writeValue(value, transValue, "caption", count, type, language, engSlug);
						break;
					}
					count++;
				}
			}
		}
	}

	private static void writeValue(JSONObject value, JSONObject transValue, String parameter, int count, String type,
			String language, String engSlug) throws JSONException, IOException {
		if (value.has(parameter) && transValue.has(parameter)) {
			write(value.get(parameter).toString(), transValue.get(parameter).toString(), count, type, language,
					engSlug);
		}

	}

	private static void writeNestedValue(JSONObject value, JSONObject transValue, String parameter, int count,
			String type, String language, String engSlug) throws JSONException, IOException {
		if (value.has(parameter) && transValue.has(parameter)) {
			write(((JSONObject) value.get(parameter)).get(parameter).toString(),
					((JSONObject) transValue.get(parameter)).get(parameter).toString(), count, type, language, engSlug);
		}

	}

	private static void write(String engText, String transText, int count, String type, String language, String engSlug)
			throws IOException {

		String engRoot = "D:\\pari_new3\\en\\";
		String langRoot = "D:\\pari_new3\\" + language + "\\";
		new File(engRoot).mkdirs();
		new File(langRoot).mkdirs();

		// String engFileName = engRoot + engSlug + "_en_" + type + "_" + count +
		// ".txt";
		// String transFileName = langRoot + engSlug + "_" + language + "_" + type + "_"
		// + count + ".txt";

		String engFileName = engRoot + engSlug + "_" + type + "_" + count + ".txt";
		String transFileName = langRoot + engSlug + "_" + type + "_" + count + ".txt";

		if (engText.equals("") || transText.equals(""))
			return;
		write(engFileName, engText);
		write(transFileName, transText);
	}

	private static void write(String fileName, String text) throws IOException {
		Writer fstream = null;
		BufferedWriter out = null;
		fstream = new OutputStreamWriter(new FileOutputStream(fileName), StandardCharsets.UTF_8);
		out = new BufferedWriter(fstream);
		text = clean(text);
		out.write(text);
		out.newLine();

		out.flush();
		out.close();
	}

	private static String clean(String text) {
		return text.replaceAll("<.*?>|&([a-z0-9]+|#[0-9]{1,6}|#x[0-9a-f]{1,6});", " ").replaceAll("\n"," ").replaceAll(" {2,}", " ").trim();
	}
}
