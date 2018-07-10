import org.postgresql.util.PSQLException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;

public class MainBank {

    private String user;
    private String password;
    private ArrayList<String> tokens;
    private Wallet wallet;

    private int difficulty;
    private int numberOfBlock;
    private float transactionFee;
    private float miningReward;
    private float maxLoan;


    public MainBank(String user, String password) throws Exception {
        this.user = user;
        this.password = password;
        wallet = new Wallet();
        tokens = new ArrayList<>();
        this.difficulty = 0;
        this.numberOfBlock = 0;
        this.transactionFee = 0;
        this.miningReward = 0;
        this.maxLoan = 0;
        saveDB();
    }

    public MainBank(String user, String password, int WalletID) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        this.user = user;
        this.password = password;
        wallet = engine.getWallet(WalletID);
        tokens = new ArrayList<>();
        String query = "select difficulty from mainManager where userName = '" + user + "' and password = '" + password + "';";
        this.difficulty = Integer.parseInt(engine.p1.getResults(query).split(",")[0]);
        query = "select numberOfBlock from mainManager where userName = '" + user + "' and password = '" + password + "';";
        this.numberOfBlock = Integer.parseInt(engine.p1.getResults(query).split(",")[0]);
        query = "select transactionFee from mainManager where userName = '" + user + "' and password = '" + password + "';";
        this.transactionFee = Float.parseFloat(engine.p1.getResults(query).split(",")[0]);
        query = "select miningReward from mainManager where userName = '" + user + "' and password = '" + password + "';";
        this.miningReward = Float.parseFloat(engine.p1.getResults(query).split(",")[0]);
        query = "select maxLoan from mainManager where userName = '" + user + "' and password = '" + password + "';";
        this.maxLoan = Float.parseFloat(engine.p1.getResults(query).split(",")[0]);
    }

    public void saveDB() throws Exception {
        String save = "insert into mainManager values ('" + user + "','" + StringUtil.encrypt(password, NoobChain.keyPair.getPublic()) + "','" + wallet.getId() +
                "','" + difficulty + "','" + numberOfBlock + "','" + transactionFee + "','" + miningReward +
                "','" + maxLoan + "');";
        engine.p1.getTable(save);
    }

    public void NumberOfBlock(int numberOfBlock) {
        this.numberOfBlock = numberOfBlock;
        String mainManager = "UPDATE mainManager SET numberofblock = " + numberOfBlock + "  where username = '" + this.user + "'";
        engine.p1.getResults(mainManager);
        System.out.println("Number of Transaction In Block: " + numberOfBlock);
    }

    public void TransactionFee(float transactionFee) {
        this.transactionFee = transactionFee;
        String mainManager = "UPDATE mainManager SET transactionFee = " + transactionFee + "  where username = '" + this.user + "'";
        engine.p1.getResults(mainManager);
        System.out.println("Transaction Fee " + transactionFee + "$");
    }

    public void BlockMiningReward(float miningReward) {
        this.miningReward = miningReward;
        String mainManager = "UPDATE mainManager SET miningReward = " + miningReward + "  where username = '" + this.user + "'";
        engine.p1.getResults(mainManager);
        System.out.println("Mining Reward " + miningReward + "$");
    }

    public void Difficulty(int difficulty) {
        this.difficulty = difficulty;
        String mainManager = "UPDATE mainManager SET difficulty = " + difficulty + "  where username = '" + this.user + "'";
        engine.p1.getResults(mainManager);
        System.out.println("Difficulty: " + difficulty);
    }

    public String addToken() {
        String token = "";
        while (true) {
            token = generateToken();
            if (!tokens.contains(token)) {
                tokens.add(token);
                break;
            }
        }
        return token;
    }

    public String generateToken() {
        String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        int count = 7;
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    public void MaxLoan(float maxLoan) {
        this.maxLoan = maxLoan;
        System.out.println("Bank Balance %" + maxLoan + " more than the Requesting Loan");
    }


    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public int getNumberOfBlock() {
        return numberOfBlock;
    }

    public float getTransactionFee() {
        return transactionFee;
    }

    public float getMiningReward() {
        return miningReward;
    }

    public float getMaxLoan() {
        return maxLoan;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public float getBalance() {
        return wallet.getBalance();
    }


}
