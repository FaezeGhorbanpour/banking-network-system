public class Bank implements Runnable {

    private String user;
    private String password;
    private String name;
    private Wallet wallet;
    private String token;

    //Bank Constructor.
    public Bank(String user, String password, String name, String token) {
        this.user = user;
        this.password = password;
        this.name = name;
        this.wallet = new Wallet();
        this.token = token;
        saveDB();
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void saveDB() {
        String save = "insert into bank values ('" + user + "','" + password + "','" + name + "','" + token + "','" + wallet.getId() + "');";
        engine.p1.getTable(save);
    }

    public String getName() {
        return name;
    }

    @Override
    public void run() {
        System.out.println("Bank Thread !");
        mine();
    }

    private void mine() {
        //TODO
    }


}
