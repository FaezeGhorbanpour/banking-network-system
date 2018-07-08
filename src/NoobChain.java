import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static java.lang.System.in;
import static java.lang.System.out;


public class NoobChain {

    public static ArrayList<Block> blockchain = new ArrayList<Block>();
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public static float minimumTransaction = 0.1f;
    public static Transaction genesisTransaction;

    public static MainBank mainManager;

    public static ArrayList<Bank> banks = new ArrayList<>();
    public static ArrayList<Customer> customers = new ArrayList<>();

    public static Bank onlineBank;
    public static Customer onlineCustomer;

    public static int type;

    public static void main(String[] args) throws IOException {

        out.println("Welcome to blockchain");
        out.println("Please enter your command");

        Scanner scanner = new Scanner(in);
        String input = scanner.nextLine();

        if (input.contains("Create Manager")) {
            String[] strings = input.split(" ");
            mainManager = new MainBank(strings[2], strings[3]);
            type = engine.signup(strings[2], strings[3], 1);
        }
        else if (input.contains("Number of Transaction In Block")) {
            String[] strings = input.split(" ");
            mainManager.NumberOfTransaction(Integer.parseInt(strings[5]));
        }
        else if (input.contains("Transaction Fee")) {
            String[] strings = input.split(" ");
            mainManager.TransactionFee(Float.parseFloat(strings[2]));
        }
        else if (input.contains("Block Mining Reward")) {
            String[] strings = input.split(" ");
            mainManager.BlockMiningReward(Float.parseFloat(strings[3]));
        }
        else if (input.contains("Difficulty")) {
            String[] strings = input.split(" ");
            mainManager.Difficulty(Integer.parseInt(strings[1]));
        }
        else if (input.equals("Generate Token")) {
            String token = mainManager.addToken();
            out.println("Token: " + token);
        }
        else if (input.contains("Bank Balance")) {
            String[] strings = input.split(" ");
            mainManager.MaxLoan(Float.parseFloat(strings[2]));
        }

        fillBank();

        while (true) {

            if (input.contains("Get")) {
                String[] strings = input.split(" ");
                GsonReader.readJson(strings[1]);
            }

            else if (input.contains("Create Bank")) {
                String[] strings = input.split(" ");
                if (mainManager.getTokens().contains(strings[5])) {
                    onlineBank = new Bank(strings[2], strings[3], strings[4], strings[5]);
                    banks.add(onlineBank);
                    type = engine.signup(strings[2], strings[3], 2);
                    Thread bankThread = new Thread(onlineBank);
                    bankThread.start();

                }
            }

            else if (input.contains("Register Customer")) {
                String[] strings = input.split(" ");
                Bank coustomerBank = getBank(strings[4]);
                onlineCustomer = new Customer(strings[2], strings[3], coustomerBank);
                customers.add(onlineCustomer);
                type = engine.signup(strings[2], strings[3], 3);
            }

            else if (input.contains("Login")) {
                String[] strings = input.split(" ");
                type = login(strings[1], strings[2]);
                if (type == 1) {
                    out.println("login successful as main manager");
                }
                else if (type == 2) {
                    out.println("login successful as bank manager");
                }
                else if (type == 3) {
                    out.println("login successful as user");
                    onlineCustomer = new Customer(strings[2], strings[3], getCustomerBank(strings[2]));
                }
                else {
                    out.println("user or password is wrong");
                }

            }

            else if (input.equals("Get Balance")) {
                if (type == 3) {
                    out.println(onlineCustomer.getBalance());
                }
                else {
                    out.println("Wrong Command");
                }
            }
            else if (input.contains("Transfer")) {
                if (type == 3) {
                    String[] strings = input.split(" ");
                    float payment = Float.parseFloat(strings[1]);
                    int receiverWalletID = Integer.parseInt(strings[3]);
                    PublicKey recevierpk = getWalletPubKey(receiverWalletID);
                    Transaction transaction = makeTransaction(payment, recevierpk, onlineCustomer.getWallet());
                    if (transaction != null)
                        Bank.rawTransaction.add(transaction);
                }
            }
            else if (input.contains("Send Transfer")) {
                String[] strings = input.split(" ");
                float payment = Float.parseFloat(strings[2]);
                String senderPUK = strings[3];
                String senderPRK = strings[4];
                String receiverPUK = strings[6];
                Wallet senderWallet = getWallet(senderPUK);
                Wallet receiverWallet = getWallet(receiverPUK);
                Transaction transaction = makeTransaction(payment, receiverWallet.publicKey, senderWallet);
                if (transaction != null)
                    Bank.rawTransaction.add(transaction);

            }
            else if (input.contains("Request Loan")) {
                if (type == 3) {
                    String[] strings = input.split(" ");
                    float loan = Float.parseFloat(strings[2]);
                    if (loan > mainManager.getMaxLoan()) {
                        out.println("More than maximum loan");
                        continue;
                    }
                    String bankName = strings[4];
                    Bank bank = getBank(bankName);
                    Transaction transaction = makeTransaction(loan, onlineCustomer.getWallet().publicKey, bank.getWallet());
                    ArrayList<Transaction> trs = new ArrayList<>();
                    trs.add(transaction);
                    Bank.bankLoan.put(bankName, trs);

                }
                else {
                    out.println("Wrong Command");
                }
            }
            else if (input.equals("Show Transaction")) {
                ArrayList<ArrayList<Transaction>> result = null;
                if (type == 3) {
                    result = getValidTransaction(onlineCustomer.getWallet());
                }
                else if (type == 2) {
                    result = getValidTransaction(onlineBank.getWallet());
                }
                else {
                    out.println("Invalid command");
                    continue;
                }
                out.println("Input Transaction:");
                printTransaction(result.get(0));
                out.println("Output Transaction:");
                printTransaction(result.get(1));

            }
            else if (input.equals("Show BlockChain")) {
                String blockchainJson = new GsonBuilder().setPrettyPrinting().create().toJson(blockchain);
                out.println("\nThe block chain: ");
                out.println(blockchainJson);
            }
            else if (input.equals("Show Invalid Transaction")) {
                ArrayList<Transaction> invalidTransaction = getInvalidTrasaction();
                out.println("Invalid Transaction: ");
                out.println(invalidTransaction);
            }
            else if (input == null) {
                break;
            }
            input = scanner.nextLine();
        }

    }

    public static void fillBank() {
        String allBank = engine.getAllBank();
        String[] allBanks = allBank.split("\\n");
        for (int i = 0; i < allBanks.length; i++) {
            String[] strings = allBanks[i].split(" ");
            Bank bank = new Bank(strings[0], strings[1], strings[2], strings[3]);
            banks.add(bank);
        }
    }

    public static Bank getBank(String name) {
        for (int i = 0; i < banks.size(); i++) {
            if (banks.get(i).getName().equals(name)) {
                return banks.get(i);
            }
        }
        return null;
    }

    public static Bank getCustomerBank(String userName) {
        String name = engine.getCustomerBank(userName);
        Bank bank = getBank(name);
        return bank;
    }

    public static int login(String user, String password) {
        int success = engine.checkLogin(user, password);
        return success;
    }

    public static Boolean isChainValid() {
        Block block;
        Block previousBlock;
        String hashTarget = new String(new char[mainManager.getDifficulty()]).replace('\0', '0');
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

        //loop through blockchain to check hashes:
        for (int i = 1; i < blockchain.size(); i++) {

            block = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            //compare registered hash and calculated hash:
            if (!block.getHash().equals(block.calculateHash())) {
                out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if (!previousBlock.getHash().equals(block.getPreviousHash())) {
                out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if (!block.getHash().substring(0, mainManager.getDifficulty()).equals(hashTarget)) {
                out.println("#This block hasn't been mined");
                return false;
            }

            //loop thru blockchains transactions:
            TransactionOutput tempOutput;
            for (int t = 0; t < block.getTransactions().size(); t++) {
                Transaction transaction = block.getTransactions().get(t);

                if (!transaction.verifySignature()) {
                    out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (transaction.getInputsValue() != transaction.getOutputsValue()) {
                    out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : transaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if (tempOutput == null) {
                        out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (input.UTXO.value != tempOutput.value) {
                        out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for (TransactionOutput output : transaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if (transaction.outputs.get(0).reciepient != transaction.reciepient) {
                    out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (transaction.outputs.get(1).reciepient != transaction.sender) {
                    out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        out.println("Blockchain is valid");
        return true;
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(mainManager.getDifficulty());
        blockchain.add(newBlock);
    }

    public static PublicKey getWalletPubKey(int walletID) {
        //TODO DB
        return null;
    }

    public static Wallet getWallet(String puk) {
        //TODO DB
        return null;
    }

    public static Transaction makeTransaction(float payment, PublicKey receiverWalletPubkey, Wallet senderWallet) {
        Transaction transaction = senderWallet.sendFunds(receiverWalletPubkey, payment);
        if (transaction == null)
            return null;
        return transaction;
    }

    public static void printTransaction(ArrayList<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            out.println(transaction);
            out.println("####   ####    ####    ####    ####");
        }
    }


    public static ArrayList<ArrayList<Transaction>> getValidTransaction(Wallet wallet) {
        ArrayList<Transaction> inputTransaction = new ArrayList<>();
        ArrayList<Transaction> outputTransaction = new ArrayList<>();

        Block block;
        Block previousBlock;


        String hashTarget = new String(new char[NoobChain.mainManager.getDifficulty()]).replace('\0', '0');

        for (int i = 1; i < blockchain.size(); i++) {

            block = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);


            if (!block.getHash().equals(block.calculateHash()) && !previousBlock.getHash().equals(block.getPreviousHash()) && !block.getHash().substring(0, NoobChain.mainManager.getDifficulty()).equals(hashTarget))
                for (Transaction transaction : block.getTransactions()) {
                    if (!transaction.verifySignature() && transaction.getInputsValue() != transaction.getOutputsValue() && transaction.outputs.get(0).reciepient != transaction.reciepient && transaction.outputs.get(1).reciepient != transaction.sender) {
                        if (transaction.sender == wallet.publicKey)
                            outputTransaction.add(transaction);
                        if (transaction.reciepient == wallet.publicKey)
                            inputTransaction.add(transaction);
                    }
                }
        }
        ArrayList<ArrayList<Transaction>> result = new ArrayList<>();
        result.add(inputTransaction);
        result.add(outputTransaction);
        return result;
    }

    public static ArrayList<Transaction> getInvalidTrasaction() {
        ArrayList<Transaction> invalidTransaction = new ArrayList<>();

        Block block;
        Block previousBlock;

        String hashTarget = new String(new char[NoobChain.mainManager.getDifficulty()]).replace('\0', '0');

        for (int i = 1; i < blockchain.size(); i++) {

            block = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);

            if (!block.getHash().equals(block.calculateHash()) || !previousBlock.getHash().equals(block.getPreviousHash()) || !block.getHash().substring(0, NoobChain.mainManager.getDifficulty()).equals(hashTarget))
                for (Transaction transaction : block.getTransactions()) {
                    if (!transaction.verifySignature() || transaction.getInputsValue() != transaction.getOutputsValue() || transaction.outputs.get(0).reciepient != transaction.reciepient || transaction.outputs.get(1).reciepient != transaction.sender) {
                        invalidTransaction.add(transaction);
                    }
                }
        }
        return invalidTransaction;
    }

}

