import org.postgresql.util.PSQLException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class engine {

    private static String host = "127.0.0.1";
    private static String port = "5434";
    private static String username = "postgres";
    private static String password = "FG1234fg";
    private static String database = "project";
    static PostgreSQLConnection p1 = new PostgreSQLConnection(host, port, username, password, database);

    public static int signup(String userName, String password, int type) throws PSQLException {
        String check = "";
        if (type == 1)
            check = "select * from mainManager where userName = '" + userName + "' and password = '" + password + "';";
        if (type == 2)
            check = "select * from bank where userName = '" + userName + "' and password = '" + password + "';";
        if (type == 3)
            check = "select * from client where userName = '" + userName + "' and password = '" + password + "';";

        String table = p1.getResults(check);

        if (!table.equals("")) {
            return type;
        }
        return 0;
    }

    public static boolean checkMainmanager() throws PSQLException {
        String mainManager = "select * from mainManager";
        String allmainManager = p1.getResults(mainManager);
        return allmainManager.equals("");
    }

    public static String login(String userName, String password) {
        String check = "select walletId from mainManager where userName = '" + userName + "' and password = '" + password + "';";
        String table = p1.getResults(check);
        if (!table.equals("")) {
            return "1," + Integer.parseInt(table.split(",")[0]);
        }

        check = "select walletId from bank where userName = '" + userName + "' and password = '" + password + "';";
        table = p1.getResults(check);
        if (!table.equals(""))
            return "2," + Integer.parseInt(table.split(",")[0]);

        check = "select walletId from client where userName = '" + userName + "' and password = '" + password + "';";
        table = p1.getResults(check);
        if (!table.equals(""))
            return "3," + Integer.parseInt(table.split(",")[0]);

        return "0";
    }

    public static String getAllBank() {
        String allBank = "select * from bank";
        return p1.getResults(allBank);
    }

    public static String getAllCustomer() {
        String allCustomer = "select * from client";
        return p1.getResults(allCustomer);
    }

    public static String getCustomerBank(String userName) {
        String bank_search = "select bankName from client where userName = '" + userName + "'";
        return p1.getResults(bank_search).split(",")[0];
    }

    public static String getBankcustomer(String bankName) {
        String res = "";
        String customer = "select username, name, id, bankName from client";
        String allCustomer = p1.getTable(customer);
        if (!allCustomer.equals("")) {
            String[] allCustomers = allCustomer.split("\\n");
            for (int i = 0; i < allCustomers.length; i++) {
                if (allCustomers[i].contains(bankName)) {
                    res += allCustomers[i] + "\n";
                }
            }
        }
        return res;
    }

    public static int getLastWalletID() {
        String walletID = "select walletId from wallet";
        String walletIDs = p1.getResults(walletID);
        if (!walletIDs.equals("")) {
            String[] strings = walletIDs.split(",");
            return Integer.parseInt(strings[strings.length - 1]);
        }
        return 0;
    }

    public static Wallet getWallet(int walletID) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String walletQuery = "select * from wallet where walletId = " + walletID + ";";
        String[] walletOut = p1.getResults(walletQuery).split(",");
        return new Wallet(Integer.parseInt(walletOut[0]), walletOut[1], walletOut[2]);
    }

    public static String getWalletPubK(int walletID) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String walletQuery = "select publicKey from wallet where walletId = " + walletID + ";";
        return p1.getResults(walletQuery).split(",")[0];
    }

    public static String getWalletPrvK(int walletID) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String walletQuery = "select privateKey from wallet where walletId = " + walletID + ";";
        return p1.getResults(walletQuery).split(",")[0];
    }

    public static Wallet getWallet(String pubKey) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String walletQuery = "select * from wallet where publicKey = " + pubKey + ";";
        String[] walletOut = engine.p1.getResults(walletQuery).split(",");
        return new Wallet(Integer.parseInt(walletOut[0]), walletOut[1], walletOut[2]);
    }
}
