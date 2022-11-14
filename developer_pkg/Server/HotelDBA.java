import java.io.*;
import java.nio.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.simple.*;
import org.json.simple.parser.*;
import java.sql.SQLException;

public class HotelDBA extends DBGate implements HotelDB {
	public static final String ADDRESS = "localhost";
	public static final String PORT = "3306";
	public static final String DBNAME = "Hotel";
	public static final String USER = "root";
	public static final String PW = "asdf1234";

	public HotelDBA() throws ClassNotFoundException {
		// TODO change the user name and password
		dbsource = new BasicDBPool(ADDRESS, PORT, DBNAME, USER, PW);
		try {
			conn = dbsource.getConnection();
			if (!conn.getMetaData().getTables(DBNAME, null, "Hotel", null).next()
					&& !conn.getMetaData().getTables(DBNAME, null, "Room", null).next()) {
				Statement stmt = conn.createStatement();
				String ddl = Files.readString(Paths.get("./data/ddl/Hotel_hotel.txt"), StandardCharsets.UTF_8);
				stmt.execute(ddl);
				ddl = Files.readString(Paths.get("./data/ddl/Hotel_room.txt"), StandardCharsets.UTF_8);
				stmt.execute(ddl);
				stmt.close();
				System.out.println("Created hotel table and Room table.");
				System.out.println("Inserting data...");

				JSONParser parser = new JSONParser();

				FileInputStream in = new FileInputStream("./data/hotel.json");
				BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));

				JSONArray a = (JSONArray) parser.parse(reader);

				PreparedStatement pstmtHotel = conn.prepareStatement("INSERT INTO Hotel VALUES (?,?,?,?)");
				PreparedStatement pstmtRoom = conn.prepareStatement("INSERT INTO Room VALUES (?,?,?,?)");

				for (Object o : a) {
					JSONObject hotel = (JSONObject) o;
					pstmtHotel.setInt(1, Integer.valueOf(String.valueOf(hotel.get("HotelID"))));
					pstmtHotel.setInt(2, Integer.valueOf(String.valueOf(hotel.get("HotelStar"))));
					pstmtHotel.setString(3, String.valueOf(hotel.get("Locality")));
					pstmtHotel.setString(4, String.valueOf(hotel.get("Street-Address")));
					pstmtHotel.execute();
					pstmtHotel.clearParameters();

					JSONArray rooms = (JSONArray) hotel.get("Rooms");

					for (Object c : rooms) {
						JSONObject roomobject = (JSONObject) c;
						pstmtRoom.setInt(1, Integer.valueOf(String.valueOf(hotel.get("HotelID"))));
						pstmtRoom.setString(2, String.valueOf(roomobject.get("RoomType")));
						pstmtRoom.setInt(3, Integer.valueOf(String.valueOf(roomobject.get("RoomPrice"))));
						pstmtRoom.setInt(4, Integer.valueOf(String.valueOf(roomobject.get("Number"))));
						pstmtRoom.execute();
						pstmtRoom.clearParameters();
					}
				}

				System.out.println("Data inserted.");
				pstmtHotel.close();
				pstmtRoom.close();
			}

			if (!conn.getMetaData().getTables(DBNAME, null, "Reservation", null).next()) {
				Statement stmt = conn.createStatement();
				String ddl = Files.readString(Paths.get("./data/ddl/Hotel_reservation.txt"), StandardCharsets.UTF_8);
				stmt.execute(ddl);
				this.close(stmt);
				System.out.println("Created reservation table.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private List<RoomInfo> selectHotelRoom(int hotelID) throws SQLException {
		List<RoomInfo> ls = new LinkedList<RoomInfo>();
		try {
			conn = dbsource.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT * FROM Room WHERE Hotel_ID = " + hotelID);
			while(rs.next()) {
				RoomInfo room = new RoomInfo(hotelID, rs.getString("RoomType"),
						rs.getInt("Number"), rs.getInt("Price"));
				ls.add(room);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return ls;
	}

	private int[] getRoomNum(int hotelID) throws SQLException {
		List<RoomInfo> roomList = selectHotelRoom(hotelID);
		int []roomNum = new int[3];
		for (RoomInfo room : roomList) {
			switch (room.getType()) {
			case "Single":
				roomNum[0] = room.getNumber();
				break;
			case "Double":
				roomNum[1] = room.getNumber();
				break;
			case "Quad":
				roomNum[2] = room.getNumber();
				break;
			default:
				break;
			}
		}
		return roomNum;
	}
	
	@Override
	public HotelInfo getHotelInfo(int hotelID) throws HotelException {
		HotelInfo hotel = null;
		try {
			if (this.isEleExist(String.valueOf(hotelID), "Hotel_ID", "Hotel")) {
				conn = dbsource.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery("SELECT * FROM Hotel WHERE Hotel_ID = " + hotelID);
				rs.next();
				hotel = new HotelInfo(hotelID, rs.getInt("Star"), rs.getString("City"),
						rs.getString("Address"));
			} else {
				throw new HotelException("Hotel ID :"+hotelID+" does not exist");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return hotel;
	}

	@Override
	public int[] availabelRooms(int hotelID, String checkIn, String checkOut) throws HotelException {
		int []avaRooms = new int[3];
		try {
			conn = dbsource.getConnection();
			avaRooms = getRoomNum(hotelID);
			String sql = "SELECT * FROM Reservation WHERE Hotel_ID = ? AND ((CheckIn >= ? AND CheckOut <= ?"
					+ ") OR (Checkin >= ? AND Checkout <= ?))";
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, hotelID);
			pstmt.setString(2, checkIn);
			pstmt.setString(3, checkIn);
			pstmt.setString(4, checkOut);
			pstmt.setString(5, checkOut);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				avaRooms[0] -= rs.getInt("Single");
				avaRooms[1] -= rs.getInt("Double");
				avaRooms[2] -= rs.getInt("Quad");
			}
			rs.close();
			this.close(pstmt);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return avaRooms;
	}

	public int calculatePrice(int hotelID, int[] roomNum, String checkin, String checkout) throws HotelException {
		int oneDayPrice = 0;
		try {
			List<RoomInfo> roomList = selectHotelRoom(hotelID);
			for (RoomInfo room : roomList) {
				switch (room.getType()) {
				case "Single":
					oneDayPrice += room.getPrice() * roomNum[0];
					break;
				case "Double":
					oneDayPrice += room.getPrice() * roomNum[1];
					break;
				case "Quad":
					oneDayPrice += room.getPrice() * roomNum[2];
					break;
				default:
					break;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		int dayDiff = countDays(checkin, checkout);
		return oneDayPrice * dayDiff;
	}
	
	private int countDays(String d1, String d2) {
		DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		LocalDate fDate = LocalDate.parse(d1, fmt);
		LocalDate sDate = LocalDate.parse(d2, fmt);
		return (int) ChronoUnit.DAYS.between(fDate, sDate);
	}
	
	private java.sql.Date toDate(String dd) throws java.text.ParseException{
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		java.util.Date pa = format.parse(dd);
		return new java.sql.Date(pa.getTime());
	}
	
	public synchronized void insertReservation(String name, int bookID,
			int hotelID, int[] roomNum, String checkin, String checkout) throws SQLException, Exception {
		int[] availableRoomNum = this.availabelRooms(hotelID, checkin, checkout);
		String errMessage = "";
		
		if (roomNum[0] > availableRoomNum[0])
			errMessage += availableRoomNum[0] + " single room(s) left\n";
		if (roomNum[1] > availableRoomNum[1])
			errMessage += availableRoomNum[1] + " double room(s) left\n";
		if (roomNum[2] > availableRoomNum[2])
			errMessage += availableRoomNum[2] + " quad room(s) left\n";
		if (errMessage.equals("") == false)
			throw new Exception("Reservation is not available:\n" + 
				errMessage.substring(0, errMessage.length() - 1));
		
		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Reservation VALUES (?,?,?,?,?,?,?,?)");
		pstmt.setInt(1, bookID);
		pstmt.setInt(2, hotelID);
		pstmt.setInt(3, roomNum[0]);
		pstmt.setInt(4, roomNum[1]);
		pstmt.setInt(5, roomNum[2]);
		pstmt.setDate(6, this.toDate(checkin));
		pstmt.setDate(7, this.toDate(checkout));
		pstmt.setString(8, name);
	}

	@Override
	public void cancelReservation(String bookID) throws HotelException {
		try {
			if (this.isEleExist(bookID, "Book_ID", "Reservation")) {
				conn = dbsource.getConnection();
				conn.createStatement().execute(
						"DELETE FROM Reservation WHERE Book_ID = " + bookID);
			} else {
				throw new HotelException("Book ID: "+bookID+" does not exist.");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addReservation(int memberID, int hotelID, int[] roomNum, String checkIn, String checkOut, String bookID, String realName)
			throws HotelException {
		// TODO Auto-generated method stub
		int[] avaRooms = availabelRooms(hotelID, checkIn, checkOut);
		for (int i = 0; i < avaRooms.length; i += 1) {
			if (roomNum[i] > avaRooms[i]) throw new HotelException("Not Enough Rooms");
		}
		try {
			conn = dbsource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(
					"INSERT INTO Reservation "
					+ "VALUES(?,?,?,?,?,?,?,?)");
			pstmt.setString(1, bookID);
			pstmt.setInt(2, hotelID);
			pstmt.setInt(3, roomNum[0]);
			pstmt.setInt(4, roomNum[1]);
			pstmt.setInt(5, roomNum[2]);
			pstmt.setString(6, checkIn);
			pstmt.setString(7, checkOut);
			pstmt.setString(8, realName);
			pstmt.execute();
			this.close(pstmt);
		} catch(SQLException e){
			e.printStackTrace();
		}
	}

	@Override
	public List<HotelInfo> listHotel(String city, String starCondition, int[] roomNum, String checkIn, String checkOut) {
		// TODO Auto-generated method stub
		List<HotelInfo> ls = new LinkedList<HotelInfo>();
		try {
			conn = dbsource.getConnection();
			//TODO check
			//ResultSet rs = conn.createStatement().executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ls;
	}
	
	private ReservationInfo getRoomNumReserv(String bookID) throws HotelException {
		ReservationInfo reserv = null;
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT * FROM Reservation WHERE Book_ID = " + bookID);
			if (rs.next()) {
				int[] roomNum = new int[3];
				roomNum[0] = rs.getInt("Single");
				roomNum[1] = rs.getInt("Double");
				roomNum[2] = rs.getInt("Quad");
				reserv = new ReservationInfo(bookID, rs.getInt("Hotel_ID"), roomNum,
						rs.getDate("checkIn").toString(), rs.getDate("checkOut").toString(), rs.getString("Customer"));
				
			} else {
				throw new HotelException("Book ID :"+bookID+" does not exist in Hotel DB.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
		return reserv;
	}

	@Override
	public void modifyReservation(String bookID, int[] roomNum, String checkIn, String checkOut) throws HotelException {
		ReservationInfo reserv = this.getRoomNumReserv(bookID);
		int []originRoomNum = reserv.getRoomNum();
		for (int i = 0; i < 3; i++) {
			if (roomNum[i] > originRoomNum[i])
				throw new HotelException("Can only reduce the number of rooms");
		}
		java.sql.Date fDate = java.sql.Date.valueOf(checkIn.replace('/','-'));
		java.sql.Date sDate = java.sql.Date.valueOf(checkOut.replace('/','-'));
		
		if (fDate.before(java.sql.Date.valueOf(reserv.getCheckIn()))  || sDate.after(java.sql.Date.valueOf(reserv.getCheckOut()))) {
			throw new HotelException("Can only shorten the reservation");
		}
		
		try {
			conn = dbsource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("UPDATE Reservation "
					+ "SET Single = ?, `Double` = ?, Quad = ?, Checkin = ?, "
					+ "Checkout = ? WHERE Book_ID = ?");
			pstmt.setInt(1, roomNum[0]);
			pstmt.setInt(2, roomNum[1]);
			pstmt.setInt(3, roomNum[2]);
			pstmt.setDate(4, fDate);
			pstmt.setDate(5, sDate);
			pstmt.setString(6, bookID);
			pstmt.execute();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}		
	}
	
}
