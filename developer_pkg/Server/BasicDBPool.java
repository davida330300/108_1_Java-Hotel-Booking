

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BasicDBPool implements DBSource {
    private String url;
    private String address;
    private String port;
    private String dbName;
    private String user;
    private String passwd;
    private int max;
    private List<Connection> connections;

    public BasicDBPool() throws ClassNotFoundException {
        this("localhost", "3306", "CashSystem", "root", "asdf1234");
    }

    public BasicDBPool(String address, String port, String dbName, String user, String passwd)
            throws ClassNotFoundException {
        this.address = address;
        this.port = port;
        this.dbName = dbName;
        this.user = user;
        this.passwd = passwd;
        this.url = "jdbc:mysql://" + this.address + ":" + this.port + "/" + this.dbName;
        max = 100;
        Class.forName("com.mysql.cj.jdbc.Driver");
        connections = new ArrayList<Connection>();
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connections.size() == 0) {
            return DriverManager.getConnection(url, user, passwd);
        } else {
            int lastIndex = connections.size() - 1;
            return connections.remove(lastIndex);
        }
    }

    public synchronized void closeConnection(Connection conn) throws SQLException {
        if (connections.size() == max) {
            conn.close();
        } else {
            connections.add(conn);
        }
    }
}
