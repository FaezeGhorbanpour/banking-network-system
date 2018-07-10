public class Customer {

    private String id;
    private String password;
    private Wallet wallet;
    private Bank bank;

    public Customer(String id, String password, Bank bank) throws Exception {
        this.id = id;
        this.password = password;
        this.bank = bank;
        this.wallet = new Wallet();
        saveDB();
    }

    public Customer(String id, String password, Bank bank, int walletID) throws Exception {
        this.id = id;
        this.password = password;
        this.bank = bank;
        this.wallet = engine.getWallet(walletID);
        saveDB();
    }

    public void saveDB() throws Exception {
        String save = "insert into client values ('" + id + "','" + StringUtil.encrypt(password, NoobChain.keyPair.getPublic()) + "','" + bank.getName() + "','" + wallet.getId() + "');";
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
