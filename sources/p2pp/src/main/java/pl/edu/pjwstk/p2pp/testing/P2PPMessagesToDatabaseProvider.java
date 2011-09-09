package pl.edu.pjwstk.p2pp.testing;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class P2PPMessagesToDatabaseProvider extends MessagesToDatabaseProvider {

	/**
	 * 
	 * @param databaseAddress
	 * @param databasePort
	 * @param login
	 * @param password
	 */
	public P2PPMessagesToDatabaseProvider(String databaseAddress, int databasePort, String login, String password) {
		super(databaseAddress, databasePort, login, password);
	}

	@Override
	public void sendMessagesToDatabase() throws IOException, SQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection dbConnection = (Connection) DriverManager.getConnection("jdbc:mysql://localhost:9999/user",
					login, password);

			Statement statement = (Statement) dbConnection.createStatement();
			ResultSet resultSet = statement.executeQuery("SELECT employee_id, name, surname FROM Employee;");

			while (resultSet.next()) {
				int id = resultSet.getInt("EMPLOYEE_ID");
				String name = resultSet.getString("NAME");
				String surname = resultSet.getString("SURNAME");

				System.out.println("" + id + "; " + name + "; " + surname);
			}

			statement.close();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
