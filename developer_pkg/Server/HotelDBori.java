import java.util.*;
import java.io.*;
import java.sql.*;
import org.json.simple.*;
import org.json.simple.parser.*;

public class HotelDBori
{
	private static final String protocol = "jdbc:sqlite:";
	private static final String filepath = "./data/";
	private static final String name = "hotel";
	private static final String fields = "(HotelID INTEGER PRIMARY KEY, HotelStar INTEGER, " +
		"City TEXT, Address TEXT, PriceSingle INTEGER, NumSingle INTEGER, " + 
		"PriceDouble INTEGER, NumDouble INTEGER, PriceQuad INTEGER, NumQuad INTEGER)";
	private Connection conn;
	private ReservationDB reservationDB; 
	
	public HotelDBori() {
		connectHotelDB();
		System.out.println("Hotel database is ready.");
	}
	
	public void connectOtherDB(ReservationDB reservationDB) {
		this.reservationDB = reservationDB;
	}
	
	public int[] availableRooms(int hotelID, int checkin, int checkout) throws Exception {
		ResultSet resultSet = selectHotel(hotelID);
		resultSet.next();
		
		int[] availableRoomNum = {resultSet.getInt("NumSingle"),
			resultSet.getInt("NumDouble"), resultSet.getInt("NumQuad")};
			
		resultSet.close();
			
		resultSet = reservationDB.selectReservation("HotelID = " + hotelID +
			" AND ((Checkin >= " + checkin + " AND Checkout <= " + checkin +
			") OR (Checkin >= " + checkout + " AND Checkout <= " + checkout + "))");

		while (resultSet.next()) {
			availableRoomNum[0] -= resultSet.getInt("Single");
			availableRoomNum[1] -= resultSet.getInt("Double");
			availableRoomNum[2] -= resultSet.getInt("Quad");
		}

		return availableRoomNum;
	}
	
	public int calculatePrice(int hotelID, int[] roomNum, int checkin, int checkout) throws Exception {
		ResultSet resultSet = selectHotel(hotelID);
		resultSet.next();
		
		return (roomNum[0] * resultSet.getInt("PriceSingle") + 
			roomNum[1] * resultSet.getInt("PriceDouble") +
			roomNum[2] * resultSet.getInt("PriceQuad")) *
			(Date.difference(checkin, checkout) + 1);
	}
	
	public List<HotelInfo> hotelList(int[] roomNum, int checkin, int checkout, String conditions) throws Exception {
		List<HotelInfo> hotelInfoList = new ArrayList<>();
		ResultSet resultSet = null;
		
		try {
			resultSet = conn.createStatement().executeQuery(
				"SELECT * FROM " + name + " WHERE " + conditions);
		} catch (Exception e) {
			throw new SQLException("Incorrect SQL format");
		}
		
		while (resultSet.next()) {
			if (roomNum[0] <= resultSet.getInt("NumSingle")
				&& roomNum[1] <= resultSet.getInt("NumDouble")
				&& roomNum[2] <= resultSet.getInt("NumQuad")) {
				hotelInfoList.add(new HotelInfo(resultSet.getInt("HotelID"), resultSet.getInt("HotelStar"),
					resultSet.getString("City"), resultSet.getString("Address"),
					(roomNum[0] * resultSet.getInt("PriceSingle") + roomNum[1] * resultSet.getInt("PriceDouble") +
					roomNum[2] * resultSet.getInt("PriceQuad")) * (Date.difference(checkin, checkout) + 1)));
			}
		}
		
		return hotelInfoList;
	}
	
	private void connectHotelDB() {
		String hotelData = "hotel.json";
		
		try {
			conn = DriverManager.getConnection(protocol + filepath + name);
		} catch (SQLException e) {
			System.err.println("Fail to connect to hotel database: " + e.getMessage());
			System.exit(0);
		}
		
		try {
			if (conn.getMetaData().getTables(null, null, name, null).next())
				return;

			Statement s = conn.createStatement();
			s.execute("CREATE TABLE " + name + fields);
				
			System.out.println("Created hotel table.");
			System.out.println("Inserting data...");
			
			JSONParser parser = new JSONParser();
			
			FileInputStream in = new FileInputStream(filepath + hotelData);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			JSONArray a = (JSONArray)parser.parse(reader);
			
			String insertStatement = null;
			
			for (Object o : a) {
				JSONObject hotel = (JSONObject)o;
				
				insertStatement = "INSERT INTO " + name + " VALUES (";
				insertStatement += Integer.valueOf(String.valueOf(hotel.get("HotelID")));
				insertStatement += ", " + Integer.valueOf(String.valueOf(hotel.get("HotelStar")));
				insertStatement += ", '" + (String)hotel.get("Locality") + "'";
				insertStatement += ", '" + (String)hotel.get("Street-Address") + "'";

				JSONArray Rooms = (JSONArray)hotel.get("Rooms");
				
				for (Object c : Rooms) {
					JSONObject roomobject = (JSONObject)c;
					
					insertStatement += ", " + Integer.valueOf(String.valueOf(roomobject.get("RoomPrice")));
					insertStatement += ", " + Integer.valueOf(String.valueOf(roomobject.get("Number")));
				}
				
				insertStatement += ")";
				s.execute(insertStatement);
			}
			
			System.out.println("Data inserted.");
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(0);
		}
	}
	
	private ResultSet selectHotel(int hotelID) throws Exception {
		return conn.createStatement().executeQuery(
			"SELECT * FROM " + name + " WHERE HotelID = " + hotelID);
	}
}
