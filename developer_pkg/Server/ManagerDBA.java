

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ManagerDBA extends DBGate implements ManagerDB {
	public static final String ADDRESS = "localhost";
	public static final String PORT = "3306";
	public static final String DBNAME = "HotelManager";
	public static final String USER = "root";
	public static final String PW = "asdf1234";
	
	private void createTable(String tableName) throws SQLException, IOException{
		conn = dbsource.getConnection();
		if (!conn.getMetaData().getTables(DBNAME, null, tableName, null).next()) {
			Statement stmt = conn.createStatement();
			String ddl = Files.readString(Paths.get("./data/ddl/Manager_"+first2low(tableName)+".txt"), StandardCharsets.UTF_8);
			stmt.execute(ddl);
			stmt.close();
			System.out.println("Created "+tableName+" table.");
		}
		this.close();
	}

	public ManagerDBA() throws ClassNotFoundException {
		dbsource = new BasicDBPool(ADDRESS, PORT, DBNAME, USER, PW);
		try {
			createTable("Advertiser");
			createTable("Ad");
			createTable("Member");
			createTable("MemberViewAd");
			createTable("Reservation");
			createTable("Fees");
			createTable("OrderHistory");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	@Override
	public OrderInfo getReservation(String bookID) throws ManagerException{
		OrderInfo order = new OrderInfo();
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM Reservation WHERE Book_ID = " + bookID);
			if (rs.next()) {
				order.bookID = bookID;
				order.hotelID = rs.getInt("Hotel_ID");
				order.roomNum[0] = rs.getInt("Single");
				order.roomNum[1] = rs.getInt("Double");
				order.roomNum[2] = rs.getInt("Quad");
				order.checkin = rs.getDate("checkIn").toString();
				order.checkout = rs.getDate("checkOut").toString();
				order.price = rs.getInt("Price");
				order.additionalInfo = rs.getString("ExpirationDate");
			} else {
				throw new ManagerException("No book ID:" +bookID+ " does not exist in Reservation");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return order;
	}
	
	@Override
	public List<OrderInfo> listReservation(int memberID) throws ManagerException {
		List<OrderInfo> ls = new LinkedList<OrderInfo>();
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT * FROM Reservation WHERE Member_ID = " + memberID);
			if (rs.next()) {
				do {
					OrderInfo order = new OrderInfo();
					order.bookID = rs.getString("Book_ID");
					order.hotelID = rs.getInt("Hotel_ID");
					order.roomNum[0] = rs.getInt("Single");
					order.roomNum[1] = rs.getInt("Double");
					order.roomNum[2] = rs.getInt("Quad");
					order.checkin = rs.getString("checkIn");
					order.checkout = rs.getString("checkOut");
					order.additionalInfo = rs.getString("ExpirationDate");
					order.price = rs.getInt("Price");
					ls.add(order);
				} while(rs.next());
			} else {
				throw new ManagerException("MemberID :"+memberID+" does not exist.");
			}
			
			dbsource.closeConnection(conn);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return ls;
	}
	
	@Override
	public List<OrderInfo> listOrder(int memberID) {
		List<OrderInfo> ls = new LinkedList<OrderInfo>();
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT * FROM OrderHistory WHERE Member_ID = " + memberID);
			dbsource.closeConnection(conn);
			while (rs.next()) {
				OrderInfo order = new OrderInfo();
				order.bookID = rs.getString("Order_ID");
				order.hotelID = rs.getInt("Hotel_ID");
				order.roomNum[0] = rs.getInt("Single");
				order.roomNum[1] = rs.getInt("Double");
				order.roomNum[2] = rs.getInt("Quad");
				order.checkin = rs.getString("checkIn");
				order.checkout = rs.getString("checkOut");
				order.price = rs.getInt("Price");
				order.additionalInfo = String.valueOf(rs.getInt("CS_ID"));
				ls.add(order);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return ls;
	}

	@Override
	public void addAccount(String realName, String username, String password,
			String email, String membership) throws ManagerException {
		try {
			conn = dbsource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(
					"INSERT INTO Member (RealName, UserName, Password, Membership, Email) "
					+ "VALUES(?,?,?,?,?)");
			pstmt.setString(1, realName);
			pstmt.setString(2, username);
			pstmt.setString(3, password);
			pstmt.setString(4, membership);
			pstmt.setString(5, email);
			pstmt.execute();
			this.close(pstmt);
		} catch (SQLException e) {
			String[] msg = e.getMessage().toString().split(" ");
			if (msg[0].contentEquals("Duplicate")) {
				//duField is duplicated field
				String duField = msg[msg.length - 1].replace("'", "");
				duField = duField.split("_UNIQUE")[0];
				throw new ManagerException(duField + " already exists.");
			}
			e.printStackTrace();
		}
		
	}
	
	@Override
	public int verifyAccount(String username, String password) throws ManagerException {
		int re = 0;
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT Password, Member_ID "+
					"FROM Member "+
					"WHERE UserName = '" + username + "'");
			dbsource.closeConnection(conn);
			if(rs.next()) {
				String pw = rs.getString("Password");
				if (!pw.equals(password)) {
					throw new ManagerException("Incorrect password.");
				}
				re = rs.getInt("Member_ID");
			} else {
				throw new ManagerException("Username not exist.");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return re;
	}

	@Override
	public int addFee(String bookID, int bankID, String creditcardID, String expirationDate, String securityCode) throws ManagerException {
		int amount = 0;
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT * FROM Reservation WHERE Book_ID = " + bookID);
			if (rs.next()) {
				amount = rs.getInt("Price");
				rs.close();
			} else {
				dbsource.closeConnection(conn);
				rs.close();
				throw new ManagerException("Book ID does not exist.");
			}
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO Fees VALUES(?,?,?,?,?)");
			pstmt.setString(1, bookID);
			pstmt.setInt(2, bankID);
			pstmt.setString(3, creditcardID);
			pstmt.setString(4, expirationDate);
			pstmt.setString(5, securityCode);
			pstmt.execute();
			this.close(pstmt);
		} catch(SQLException e){
			e.printStackTrace();
		}
		return amount;
	}
	
	public int getHotelID(String bookID) throws ManagerException {
		int hotelID = 0;
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery(
					"SELECT Hotel_ID FROM Reservation WHERE Book_ID = " + bookID);
			if (rs.next()) {
				hotelID = rs.getInt(1);
			} else {
				throw new ManagerException("No bookID exist in Reservation");
			}
			dbsource.closeConnection(conn);
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return hotelID;
	}

	@Override
	public void cancelReservation(String bookID) throws ManagerException {
		try {
			if (this.isEleExist(bookID, "Book_ID", "Reservation")) {
				conn = dbsource.getConnection();
				conn.createStatement().execute(
						"DELETE FROM Reservation WHERE Book_ID = " + bookID);
			} else {
				throw new ManagerException("Book ID: "+bookID+" does not exist.");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public synchronized String addReservation(int memberID, int hotelID, int[] roomNum, String checkIn, String checkOut, java.sql.Date expDate, int price) throws ManagerException {
		String bookID = null;
		try {
			conn = dbsource.getConnection();
			SimpleDateFormat dformat = new SimpleDateFormat("yyyy/MM/dd");
			java.util.Date inDate = dformat.parse(checkIn);
			java.util.Date outDate = dformat.parse(checkOut);
			PreparedStatement pstmt = conn.prepareStatement(
					"INSERT INTO Reservation "
					+ "(Member_ID, Hotel_ID, Single, `Double`, Quad, CheckIn, CheckOut, Price, ExpirationDate) "
					+ "VALUES(?,?,?,?,?,?,?,?,?)");
			pstmt.setInt(1, memberID);
			pstmt.setInt(2, hotelID);
			pstmt.setInt(3, roomNum[0]);
			pstmt.setInt(4, roomNum[1]);
			pstmt.setInt(5, roomNum[2]);
			pstmt.setDate(6, new java.sql.Date(inDate.getTime()));
			pstmt.setDate(7, new java.sql.Date(outDate.getTime()));
			pstmt.setInt(8, price);
			pstmt.setDate(9, expDate);
			pstmt.execute();
			
			ResultSet rs = conn.createStatement().executeQuery("SELECT LAST_INSERT_ID()");
			if(rs.next()) {
				bookID = rs.getString(1);
			}
			this.close(pstmt);
		} catch(SQLException e){
			e.printStackTrace();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bookID;
	}

	@Override
	public void modifyReservation(String bookID, int[] roomNum, String checkIn, String checkOut) throws ManagerException {
		OrderInfo order = this.getReservation(bookID);
		int[] originRoomNum = order.roomNum;
		
		for (int i = 0; i < 3; i++) {
			if (roomNum[i] > originRoomNum[i])
				throw new ManagerException("Can only reduce the number of rooms");
		}
		java.sql.Date fDate = java.sql.Date.valueOf(checkIn.replace('/','-'));
		java.sql.Date sDate = java.sql.Date.valueOf(checkOut.replace('/','-'));
		
		if (fDate.before(java.sql.Date.valueOf(order.checkin.replace('/','-')))  || sDate.after(java.sql.Date.valueOf(order.checkout.replace('/','-')))) {
			throw new ManagerException("Can only shorten the reservation");
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

	@Override
	public void insertOrder(OrderInfo order, int memberID, int bankID) {
		try {
			conn = dbsource.getConnection();
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO OrderHistory VALUES(?,?,?,?,?,?,?,?,?,?)");
			pstmt.setInt(1, Integer.valueOf(order.bookID));
			pstmt.setInt(2, memberID);
			pstmt.setInt(3, order.hotelID);
			pstmt.setInt(4, order.roomNum[0]);
			pstmt.setInt(5, order.roomNum[1]);
			pstmt.setInt(6, order.roomNum[2]);
			pstmt.setDate(7, java.sql.Date.valueOf(order.checkin));
			pstmt.setDate(8, java.sql.Date.valueOf(order.checkout));
			pstmt.setInt(9, order.price);
			pstmt.setInt(10, bankID);
			pstmt.execute();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			this.close();
		}
	}
	
	@Override
	public int getMemIDFromRes(String bookID) {
		int memberID = 0;
		try {
			conn = dbsource.getConnection();
			ResultSet rs = conn.createStatement().executeQuery("SELECT Member_ID FROM Reservation WHERE book_ID = " + bookID);
			rs.next();
			memberID = rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return memberID;
	}
}
