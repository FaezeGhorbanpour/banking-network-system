public class engine {

    static String host = "127.0.0.1";
    static String port = "5432";
    static String username = "postgres";
    static String password = "1234";
    static String database = "project";
    static PostgreSQLConnection p1 = new PostgreSQLConnection(host, port, username, password, database);

    public static int checkLogin(String userName, String password) {
        String check = "select * from mainManager";
        if (p1.getTable(check).contains("| " + userName + "                         | " + password))
            return 1;
        check = "select * from bank";
        if (p1.getTable(check).contains("| " + userName + "                         | " + password))
            return 2;
        check = "select * from client";
        if (p1.getTable(check).contains("| " + userName + "                         | " + password))
            return 3;
        return 0;
    }

    public static int signup(String userName, String password, int type) {
        String signUp = "";
        if (type == 1) {
            signUp = "insert into mainManager values ('" + userName + "','" + password + "');";
        }
        else if (type == 2) {
            signUp = "insert into bank values ('" + userName + "','" + password + "');";
        }
        else if (type == 3) {
            signUp = "insert into client values ('" + userName + "','" + password + "');";
        }
        p1.getTable(signUp);
        return checkLogin(userName, password);
    }

    public static String getAllBank() {
        String allBank = "select * from manager";
        System.out.println(p1.getTable(allBank));
        return p1.getTable(allBank);
    }

    public static String getCustomerBank(String userName) {
        String bank_search = "select name from client where userName = '" + userName + "'";
        System.out.println(p1.getTable(bank_search));
        return p1.getTable(bank_search);
    }

}