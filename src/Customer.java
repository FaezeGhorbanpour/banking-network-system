public class Customer {

    private String id;
    private String password;
    private Wallet wallet;
    private Bank bank;

    public Customer(String id, String password, Bank bank) {
        if (checkUser(id)) {
            this.id = id;
            this.password = password;
            this.bank = bank;
            this.wallet = new Wallet();
            saveDB();
        }
    }


    public boolean checkUser(String id) {
        return true;
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

}
