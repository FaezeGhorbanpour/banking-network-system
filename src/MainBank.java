import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Random;

public class MainBank {

    private String user;
    private String password;
    private ArrayList<String> tokens;

    private int difficulty;
    private int numberOfBlock;
    private float transactionFee;
    private float miningReward;
    private float maxLoan;


    public MainBank(String user, String password) {
        this.user = user;
        this.password = password;
        tokens = new ArrayList<>();
        saveDB();
    }

    public void saveDB() {

    }

    public void NumberOfBlock(int numberOfBlock) {
        this.numberOfBlock = numberOfBlock;
        System.out.println("Number of Transaction In Block: " + numberOfBlock);
    }

    public void TransactionFee(float transactionFee) {
        this.transactionFee = transactionFee;
        System.out.println("Transaction Fee" + transactionFee);
    }

    public void BlockMiningReward(float miningReward) {
        this.miningReward = miningReward;
        System.out.println("Mining Reward" + miningReward);
    }

    public void Difficulty(int difficulty) {
        this.difficulty = difficulty;
        System.out.println("Difficulty" + difficulty);
    }

    public String addToken() {
        String token = "";
        boolean notAdded = false;
        while (!notAdded)
            token = generateToken();
        if (!tokens.contains(token)) {
            tokens.add(token);
            notAdded = true;
        }
        return token;
    }

    public String generateToken() {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
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
}
