

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class DBGate {
	protected DBSource dbsource;
    protected Connection conn;

    public DBGate() {

    }
    
    public DBGate(String address, String port, String dbName, String user, String pw) throws ClassNotFoundException {
        dbsource = new BasicDBPool(address, port, dbName, user,pw);
    }
    
    protected String first2low(String s) {
		char c[] = s.toCharArray();
		c[0] = Character.toLowerCase(c[0]);
		return new String(c);
	}
	
	protected boolean isEleExist(String element, String column, String table) {
		boolean re = false;
		PreparedStatement pstmt = null;
		try {
			conn = dbsource.getConnection();
			pstmt = conn.prepareStatement("SELECT COUNT(1) FROM "+table+" WHERE ?=?");
			pstmt.setString(1, column);
			pstmt.setString(2, element);

			ResultSet result = pstmt.executeQuery();
			if (result.next()) {
				re = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
                    e.printStackTrace();
                    return false;
				}
			}
			if (conn != null) {
				try {
					dbsource.closeConnection(conn);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
                    e.printStackTrace();
                    return false;
				}
			}
		}
		return re;
	}
	
	protected void close() {
		if (conn != null) {
			try {
				dbsource.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	protected void close(Statement stmt) {
		if (stmt != null) {
			try {
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (conn != null) {
			try {
				dbsource.closeConnection(conn);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	protected void close(PreparedStatement pstmt) throws SQLException {
		if (pstmt != null) {
			pstmt.close();
		}
		if (conn != null) {
			dbsource.closeConnection(conn);
		}
	}
}
