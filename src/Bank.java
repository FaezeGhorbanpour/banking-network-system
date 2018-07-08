import java.util.ArrayList;
import java.util.HashMap;

public class Bank implements Runnable {

    private String user;
    private String password;
    private String name;
    private Wallet wallet;
    private String token;
    public static int status = 0; // 0 not filled, 1 bank take it
    public static ArrayList<Transaction> rawTransaction = new ArrayList<>();
    public static ArrayList<Transaction> secondRawTransaction = new ArrayList<>();
    public static HashMap<String, ArrayList<Transaction>> bankLoan = new HashMap<>();

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
        while (true) {
            int numberOfTrasaction = NoobChain.mainManager.getNumberOfTransaction() - 1;
            if (status == 0 && (rawTransaction.size() == numberOfTrasaction || (rawTransaction.size() + bankLoan.get(name).size() == numberOfTrasaction))) {
                status = 1;
                ArrayList<Transaction> transactionCopy = rawTransaction;
                rawTransaction = (ArrayList<Transaction>) secondRawTransaction.clone();
                secondRawTransaction = new ArrayList<>();
                status = 0;

                float firstValue = NoobChain.mainManager.getMiningReward();
                Transaction firstTransaction = NoobChain.makeTransaction(firstValue, wallet.publicKey, NoobChain.mainManager.getWallet());

                Block lastBlock = NoobChain.blockchain.get(NoobChain.blockchain.size() - 1);
                Block block = new Block(lastBlock.getHash());
                ArrayList<Transaction> newTrs = new ArrayList<>();
                newTrs.add(firstTransaction);

            }
            if (status == 2)
                break;
        }
    }


}
