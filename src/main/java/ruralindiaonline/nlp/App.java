package ruralindiaonline.nlp;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.json.JSONObject;

import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

/**
 * Hello world!
 *
 */
public class App {
	public static void main(String[] args) throws RazorpayException, SQLException, IOException {
		Properties prop = new Properties();
		prop.load(App.class.getResourceAsStream("/application.properties"));
		RazorpayClient razorpayClient = new RazorpayClient(prop.getProperty("key"), prop.getProperty("secret"));
		List<Payment> payments = razorpayClient.Payments.fetchAll();
		System.out.println(payments.size());
		
		for (Payment p : payments) {
			Object notes = p.get("notes");
			if (notes instanceof JSONObject) {
				System.out.println(((JSONObject) notes).get("customer_pan"));
			}
		}
		
		
		posgres(prop);

	}

	public static void posgres(Properties prop) throws SQLException {

		ResultSet rs = DriverManager
				.getConnection("jdbc:postgresql://" + prop.getProperty("url") + "/" + prop.getProperty("db"),
						prop.getProperty("username"), prop.getProperty("password"))
				.createStatement().executeQuery(prop.getProperty("query"));
		while (rs.next()) {
			System.out.println(rs.getString(1));
		}
	}
}
