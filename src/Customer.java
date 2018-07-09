import org.postgresql.util.PSQLException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class Customer {

    private String id;
    private String password;
    private Wallet wallet;
    private Bank bank;

    public Customer(String id, String password, Bank bank) throws PSQLException {
        this.id = id;
        this.password = password;
        this.bank = bank;
        this.wallet = new Wallet();
        saveDB();
    }

    public Customer(String id, String password, Bank bank, int walletID) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeySpecException, NoSuchAlgorithmException {
        this.id = id;
        this.password = password;
        this.bank = bank;
        this.wallet = engine.getWallet(walletID);
        saveDB();
    }

    public void saveDB() {
        String save = "insert into client values ('" + id + "','" + password + "','" + bank.getName() + "','" + wallet.getId() + "');";
        engine.p1.getTable(save);
    }

    public float getBalance() {
        return wallet.getBalance();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getId() {
        return id;
    }


    public Bank getBank() {
        return bank;
    }
}
