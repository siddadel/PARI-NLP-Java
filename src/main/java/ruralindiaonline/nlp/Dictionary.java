package ruralindiaonline.nlp;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.razorpay.RazorpayException;

public class Dictionary {

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
			JSONArray jsonArray = new JSONArray(rs.getString("content"));
			String language = rs.getString("language");
			String slug = rs.getString("slug");
			String title = rs.getString("title");
			String strap = rs.getString("strap");

			addToDictionary(title, slug, language);
			addToDictionary(strap, slug, language);

			Iterator<Object> i = jsonArray.iterator();

			int count = 0;
			while (i.hasNext()) {

				JSONObject object = (JSONObject) i.next();

				String type = object.getString("type");
				JSONObject value = (JSONObject) object.get("value");
				switch (type) {
				case "paragraph":
					addToDictionary(value.get("content").toString(), slug, language);
					break;
				case "image_with_quote_and_paragraph":
					addToDictionary(((JSONObject) value.get("quote")).get("quote").toString(), slug, language);
					break;
				case "columnar_image_with_text":
					addToDictionary(value.get("caption").toString(), slug, language);
					addToDictionary(((JSONObject) value.get("content")).get("content").toString(), slug, language);
					break;
				case "video_with_quote":
					addToDictionary(value.get("quote").toString(), slug, language);
					break;
				case "paragraph_with_page":
					addToDictionary(((JSONObject) value.get("content")).get("content").toString(), slug, language);
					break;
				case "full_width_image":
					addToDictionary(value.get("caption").toString(), slug, language);
					break;
				}
				count++;
			}
		}
		rs.close();
		writeDictionary(dictionary);
	}

	private static HashMap<String, Word> dictionary = new HashMap<>();

	private static void addToDictionary(String text, String slug, String language) {
		text = text.replace("<p class=\"MsoNormal\">", " ");
		text = text.replace("<p>", " ");
		text = text.replace("<i>", " ");
		text = text.replace("</p>", " ");
		text = text.replace("</i>", " ");
		
		String[] tokens = text.replaceAll("[^a-zA-Zऀ-ॿঀ-৻ଁ-୍\u0600-\u077f ]", "").toLowerCase().split("\\s+");
		for (String s : tokens) {
			Word w = dictionary.get(s);
			if (w == null) {
				w = new Word();
				dictionary.put(s, w);
			}
			w.count++;
			w.slugs.add(slug);
			w.language = language;
		}
	}
	

	private static void writeDictionary(HashMap<String, Word> dictionary) throws IOException {
		FileWriter fw = new FileWriter("D:\\pari\\dictionary.csv");
		Set<String> keys = dictionary.keySet();
		for (String k : keys) {
			Word w = dictionary.get(k);
				fw.write("\"" + k + "\"");
				fw.write(",");
				fw.write("\"" + w.count + "\"");
//				fw.write(",");
//				fw.write("\"" + w.language + "\"");
//				for (String s : w.slugs) {
//					fw.write(",");
//					fw.write("\"" + s + "\"");
//				}
				fw.write("\n");
		}
		fw.flush();
		fw.close();
	}

}

class Word {
	int count = 0;
	ArrayList<String> slugs = new ArrayList<String>();
	String language;

	public String toString() {
		return count + " " + slugs.toString();
	}
}