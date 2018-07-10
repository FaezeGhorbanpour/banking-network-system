import java.nio.charset.StandardCharsets;
import java.sql.*;

class PostgreSQLConnection {

    public String host;
    public String port;
    public String username;
    public String password;
    public String database;
    private Connection connection;

    public PostgreSQLConnection(String host, String port, String username, String password, String database) {
        super();
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.database = database;
    }

    public void checkDemo(String table, String pkColumn) {
        try {
            this.connect();
            Statement stmt = null;
            String query = "SELECT * FROM " + table;

            stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            System.out.println("Column " + pkColumn);
            while (rs.next()) {
                String id = new String(rs.getBytes(pkColumn), StandardCharsets.UTF_8);
                System.out.println("| Column " + id + " |");
            }
            this.disconnect();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    // ,,,,
    public String getResults(String sqlQuery) {
        try {
            String result = "";
            this.connect();
            Statement stmt = null;
            stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);
            ResultSetMetaData rsMeta = rs.getMetaData();
            int count = rsMeta.getColumnCount();
            int i, j = 1;
            while (rs.next()) {
                i = 1;
                while (i <= count) {
                    String formatedValue = new String(rs.getBytes(i), StandardCharsets.UTF_8);
                    result += formatedValue + ",";
                    i++;
                }
                result += "?";
            }
            this.disconnect();
            return result;
        } catch (Exception e) {
//            e.printStackTrace(System.out);
            return "";
        }
    }

    public String getTable(String sqlQuery) {
        try {
            String result = "";
            this.connect();
            Statement stmt = null;
            stmt = this.connection.createStatement();
            ResultSet rs = stmt.executeQuery(sqlQuery);
            ResultSetMetaData rsMeta = rs.getMetaData();
            int count = rsMeta.getColumnCount();
            int i, j = 1;
            result += "\n| ";
            while (j <= count) {
                String format = "%1$-" + rsMeta.getColumnDisplaySize(j) + "s";
                String formatedValue = String.format(format, rsMeta.getColumnLabel(j));
                result += formatedValue + "| ";
                j++;
            }
            result += "\n" + new String(new char[result.length()]).replace("\0", "-");
            while (rs.next()) {
                i = 1;
                result += "\n| ";
                while (i <= count) {
                    String format = "%1$-" + rsMeta.getColumnDisplaySize(i) + "s";
                    String formatedValue = String.format(format, new String(rs.getBytes(i), StandardCharsets.UTF_8));
                    result += formatedValue + "| ";
                    i++;
                }
            }
            this.disconnect();
            return result;
        } catch (Exception e) {
//            e.printStackTrace(System.out);
            return "";
        }
    }

    private void connect() throws Exception {
        Class.forName("org.postgresql.Driver");
        this.connection = null;
        this.connection = DriverManager.getConnection(
                "jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
    }

    private void disconnect() throws Exception {
        if (this.connection != null) {
            this.connection.close();
            this.connection = null;
        }
    }
}
