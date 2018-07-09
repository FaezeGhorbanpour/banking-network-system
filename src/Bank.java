import org.postgresql.util.PSQLException;

import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;

public class Bank implements Runnable {

    private String user;
    public String password;
    private String name;
    private Wallet wallet;
    private String token;

    public static int status = 0; // 0 not filled, 1 bank take it
    public static ArrayList<Transaction> rawTransaction = new ArrayList<>();
    public static ArrayList<Transaction> secondRawTransaction = new ArrayList<>();
    public static HashMap<String, ArrayList<Transaction>> bankLoan = new HashMap<>();
    public static HashMap<PublicKey, ArrayList<Transaction>> invaledTransaction = new HashMap<>();
    public static Wallet nullWallet = new Wallet(-1);


    //Bank Constructor.
    public Bank(String user, String password, String name, String token) throws PSQLException {
        this.user = user;
        this.password = password;
        this.name = name;
        this.wallet = new Wallet(10);
        this.token = token;
        saveDB();
    }

    public Bank(String user, String password, int walletID) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.user = user;
        this.password = password;
        String query = "select bankName from bank where userName = '" + user + "' and password = '" + password + "';";
        this.name = engine.p1.getResults(query).split(",")[0];
        this.wallet = engine.getWallet(walletID);
        query = "select token from bank where userName = '" + user + "' and password = '" + password + "';";
        this.token = engine.p1.getResults(query).split(",")[0];
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
        int numberOfTransaction = NoobChain.mainManager.getNumberOfBlock() - 1;
        int number = 0;
        System.out.println("Bank Thread !");
        while (true) {
            if (status == 0 && (rawTransaction.size() == numberOfTransaction - number || (bankLoan.get(name) != null && rawTransaction.size() + bankLoan.get(name).size() == numberOfTransaction - number))) {
                status = 1;
                ArrayList<Transaction> transactionCopy = rawTransaction;
                rawTransaction = (ArrayList<Transaction>) secondRawTransaction.clone();
                secondRawTransaction = new ArrayList<>();
                status = 0;
                if (bankLoan.get(name) != null)
                    transactionCopy.addAll(bankLoan.get(name));

                Block block = new Block(null);
                float firstValue = NoobChain.mainManager.getMiningReward();
                Transaction firstTransaction = NoobChain.makeTransaction(firstValue, wallet.getPublicKey(), nullWallet);

                if (!block.addTransaction(firstTransaction)) {
                    System.out.println("ّFirst Transaciton Failed!");
                }
                for (Transaction transaction : transactionCopy) {
                    if (!block.addTransaction(transaction)) {
                        if (invaledTransaction.get(transaction.sender) == null) {
                            ArrayList<Transaction> newTrs = new ArrayList<>();
                            newTrs.add(transaction);
                            invaledTransaction.put(transaction.sender, newTrs);
                        }
                        else {
                            invaledTransaction.get(transaction.sender).add(transaction);
                        }
                        if (invaledTransaction.get(transaction.reciepient) == null) {
                            ArrayList<Transaction> newTrs = new ArrayList<>();
                            newTrs.add(transaction);
                            invaledTransaction.put(transaction.reciepient, newTrs);
                        }
                        else {
                            invaledTransaction.get(transaction.reciepient).add(transaction);
                        }
                    }
                    else
                        number++;

                }
                if (numberOfTransaction == NoobChain.mainManager.getNumberOfBlock() - 1) {
                    number = 0;
                    Block lastBlock = NoobChain.blockchain.get(NoobChain.blockchain.size() - 1);
                    block.setPreviousHash(lastBlock.getHash());
                    block.mineBlock(NoobChain.mainManager.getDifficulty());
                }



            }
            if (status == 2)
                break;
        }
    }

    public Wallet getWallet() {
        return wallet;
    }

    public String getUser() {
        return user;
    }

    public float getBalance() {
        return wallet.getBalance();
    }
}
