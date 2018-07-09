import com.google.gson.GsonBuilder;
import org.postgresql.util.PSQLException;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
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

    public static int type = 0;

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, PSQLException {

        out.println("Welcome to blockchain");
        out.println("Please enter your command");

        Scanner scanner = new Scanner(in);
        String input;
        fillBank();
        while (true) {
            input = scanner.nextLine();

            if (input.contains("Create Manager")) {
                String[] strings = input.split(" ");
                if (!engine.checkMainmanager()) {
                    System.out.println("can not sign up, already have main manager");
                }
                else {
                    mainManager = new MainBank(strings[2], strings[3]);
                    type = engine.signup(strings[2], strings[3], 1);
                    if (type == 1) {
                        System.out.println("successful sign up");
                    }
                }
            }
            if (type == 1) {

                if (input.contains("Number of Transaction In Block")) {
                    String[] strings = input.split(" ");
                    mainManager.NumberOfBlock(Integer.parseInt(strings[5]));
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
            }
            else if (input.contains("Get")) {
                String[] strings = input.split(" ");
                GsonReader.readJson(strings[1]);
            }


            else if (input.equals("Show PubK PriK")) {
                int walletID = 0;
                if (type == 1) {
                    walletID = mainManager.getWallet().getId();
                }
                else if (type == 2) {
                    walletID = onlineBank.getWallet().getId();
                }
                else if (type == 3) {
                    walletID = onlineCustomer.getWallet().getId();
                }
                System.out.println("public key: " + engine.getWalletPubK(walletID));
                System.out.println("Private key: " + engine.getWalletPrvK(walletID));
            }
            else if (input.contains("Create Bank")) {
                String[] strings = input.split(" ");
                if (mainManager.getTokens().contains(strings[5])) {
                    onlineBank = new Bank(strings[2], strings[3], strings[4], strings[5]);
                    banks.add(onlineBank);
                    type = engine.signup(strings[2], strings[3], 2);
                    Thread bankThread = new Thread(onlineBank);
                    bankThread.start();
                    System.out.println("successful sign up");
                    System.out.println("Wallet ID: " + onlineBank.getWallet().getId());

                }
            }

            else if (input.contains("Register Customer")) {
                String[] strings = input.split(" ");
                Bank customerBank = getBank(strings[4]);
                onlineCustomer = new Customer(strings[2], strings[3], customerBank);
                customers.add(onlineCustomer);
                type = engine.signup(strings[2], strings[3], 3);
                System.out.println("successful sign up");
                System.out.println("Wallet ID: " + onlineCustomer.getWallet().getId());
            }

            else if (input.contains("Login")) {
                String[] strings = input.split(" ");
                String[] ouput = engine.login(strings[1], strings[2]).split(",");
                type = Integer.parseInt(ouput[0]);
                if (type == 1) {
                    mainManager = new MainBank(strings[1], strings[2], Integer.parseInt(ouput[1]));
                    System.out.println("login successful as main manager");
                }
                else if (type == 2) {
                    for (int i = 0; i < banks.size(); i++) {
                        if (banks.get(i).getUser().equals(strings[1])) {
                            onlineBank = banks.get(i);
                            break;
                        }
                }
                    Thread bankThread = new Thread(onlineBank);
                    bankThread.start();
                    System.out.println("login successful as bank manager");
                }
                else if (type == 3) {
                    for (int i = 0; i < customers.size(); i++) {
                        if (customers.get(i).getId().equals(strings[1])) {
                            onlineCustomer = customers.get(i);
                            break;
                        }
                    }
                    System.out.println("login successful as user");
                }
                else {
                    System.out.println("user or password is wrong");
                }

            }


            else if (input.equals("Get Balance")) {
                if (type == 3) {
                    out.println(onlineCustomer.getBalance());
                }
                else if (type == 2) {
                    System.out.println(onlineBank.getBalance());
                }
                else if (type == 1) {
                    System.out.println(mainManager.getBalance());
                }
                else {
                    System.out.println("Permission Deny");
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
                ArrayList<Transaction> invalidTransaction;
                if (type == 3)
                    invalidTransaction = getInvalidTransactionCustomer(onlineCustomer);
                else if (type == 2)
                    invalidTransaction = getInvalidTransactionBank(onlineBank);
                else
                    invalidTransaction = getAllInvalidTransaction();
                out.println("Invalid Transaction: ");
                out.println(invalidTransaction);
            }
            else if (input.contains("Show Customers")) {
                if (type == 3) {
                    System.out.println("Table of Customer bank: " + onlineBank.getName());
                    System.out.println(engine.getBankcustomer(onlineBank.getName()));
                }
                else {
                    System.out.println("Permission Deny");
                }
            }

            else if (input.equals("Logout")) {
                type = 0;
            }
            else if (input == null) {
                break;
            }
        }

    }

    private static ArrayList<Transaction> getInvalidTransactionBank(Bank bank) {
        ArrayList<Transaction> invalidTransaction = new ArrayList<>();
        for (Customer customer : customers)
            if (customer.getBank().getUser().equals(bank.getUser()))
                invalidTransaction.addAll(getInvalidTransactionCustomer(customer));
        return invalidTransaction;
    }

    private static ArrayList<Transaction> getAllInvalidTransaction() {
        ArrayList<Transaction> transactions = new ArrayList<>();
        for (HashMap.Entry<PublicKey, ArrayList<Transaction>> entry : Bank.invaledTransaction.entrySet()) {
            transactions.addAll(entry.getValue());
        }
        return transactions;
    }

    public static void fillBank() throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String allBank = engine.getAllBank();
        if (!allBank.equals("")) {
            String[] allBanks = allBank.split("\\n");
            for (int i = 0; i < allBanks.length; i++) {
                String[] strings = allBanks[i].split(",");
                Bank bank = new Bank(strings[0], strings[1], Integer.parseInt(strings[4]));
                banks.add(bank);
            }
        }
    }

    public static void fillCustomer() throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        String allCustomer = engine.getAllCustomer();
        if (!allCustomer.equals("")) {
            String[] allCustomers = allCustomer.split("\\n");
            for (int i = 0; i < allCustomers.length; i++) {
                String[] strings = allCustomers[i].split(",");
                Customer customer = new Customer(strings[0], strings[1], getBank(strings[2]), Integer.parseInt(strings[3]));
                customers.add(customer);
            }
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
        return getBank(name);
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
                        if (transaction.sender == wallet.getPublicKey())
                            outputTransaction.add(transaction);
                        if (transaction.reciepient == wallet.getPublicKey())
                            inputTransaction.add(transaction);
                    }
                }
        }
        ArrayList<ArrayList<Transaction>> result = new ArrayList<>();
        result.add(inputTransaction);
        result.add(outputTransaction);
        return result;
    }

    public static ArrayList<Transaction> getInvalidTransactionCustomer(Customer customer) {
        ArrayList<Transaction> invalidTransaction = new ArrayList<>();

        if (Bank.invaledTransaction.get(customer.getWallet().publicKey) != null) {
            return Bank.invaledTransaction.get(customer.getWallet().publicKey);
        }
        return invalidTransaction;
    }

    public static ArrayList<ArrayList<Transaction>> getValidTransactionBanksCustomer(Bank bank) {
        ArrayList<ArrayList<Transaction>> validTransaction = new ArrayList<>();
        ArrayList<Transaction> input = new ArrayList<>();
        ArrayList<Transaction> output = new ArrayList<>();
        for (Customer customer : customers) {
            if (customer.getBank().getUser().equals(bank.getUser())) {
                ArrayList<ArrayList<Transaction>> customerTransaction = getValidTransaction(customer.getWallet());
                input.addAll(customerTransaction.get(0));
                output.addAll(customerTransaction.get(1));

            }

        }
        validTransaction.add(input);
        validTransaction.add(output);
        return validTransaction;
    }
}

