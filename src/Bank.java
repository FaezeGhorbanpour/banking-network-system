public class Bank {

    public String user;
    public String password;
    public String name;
    private Wallet wallet;
    private String token;

    public Bank() {

    }

    //Bank Constructor.
    public Bank(String user, String password, String name, String token) {
        this.user = user;
        this.password = password;
        this.name = name;
        this.wallet = new Wallet();
        this.token = token;
        saveDB();
    }

    public void saveDB() {

    }
}
