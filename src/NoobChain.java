import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

//import java.util.Base64;


public class NoobChain {

	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	public static HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	public static float minimumTransaction = 0.1f;
	public static Wallet walletA;
	public static Wallet walletB;
	public static Transaction genesisTransaction;

	public static MainBank mainManager;

	public static ArrayList<Bank> banks = new ArrayList<>();
	public static ArrayList<Customer> customers = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		Scanner scanner = new Scanner(System.in);

		while (true) {
			String input = scanner.nextLine();

			if (input.contains("Create Manager")) {
				String[] strings = input.split(" ");
				mainManager = new MainBank(strings[2], strings[3]);
			}

			if (input.contains("Number of Transaction In Block")) {
				String[] strings = input.split(" ");
				mainManager.NumberOfBlock(Integer.parseInt(strings[5]));
			}

			if (input.contains("Transaction Fee")) {
				String[] strings = input.split(" ");
				mainManager.TransactionFee(Float.parseFloat(strings[2]));
			}

			if (input.contains("Block Mining Reward")) {
				String[] strings = input.split(" ");
				mainManager.BlockMiningReward(Float.parseFloat(strings[3]));
			}

			if (input.contains("Difficulty")) {
				String[] strings = input.split(" ");
				mainManager.Difficulty(Integer.parseInt(strings[1]));
			}


			if (input.equals("Generate Token")) {
				String token = mainManager.addToken();
				System.out.println("Token: " + token);
			}

			if (input.contains("Bank Balance")) {
				String[] strings = input.split(" ");
				mainManager.MaxLoan(Float.parseFloat(strings[2]));
			}

			if (input.contains("Get")) {
				String[] strings = input.split(" ");
				readJson(strings[1]);
			}

			if (input.contains("Create Bank")) {
				String[] strings = input.split(" ");
				if (mainManager.getTokens().contains(strings[5])) {
					Bank bank = new Bank(strings[2], strings[3], strings[4], strings[5]);
					banks.add(bank);
				}
			}

			if (input.contains("Register Customer")) {
				String[] strings = input.split(" ");
				Bank coustomerBank = getBank(strings[4]);
				Customer customer = new Customer(strings[2], strings[3], coustomerBank);
				customers.add(customer);
			}

			if (input.contains("Login")) {
				String[] strings = input.split(" ");
				if (login(strings[1], strings[2])) {
					System.out.println("login successful");
				}
				else {
					System.out.println("user or password is wrong");
				}

			}
			if (input == null) {
				break;
			}
		}

	}

	public static Bank getBank(String name) {
		Bank bank = new Bank();
		return bank;
	}

	public static boolean login(String user, String password) {

		return true;
	}

	public static void readJson(String fileName) throws IOException {
		try (Reader reader = new InputStreamReader(new FileInputStream(fileName))) {
			Gson gson = new GsonBuilder().create();
//            Person p = gson.fromJson(reader, Person.class);
//            System.out.println(p);
		}
	}

	public static Boolean isChainValid() {
		Block currentBlock;
		Block previousBlock;
		String hashTarget = new String(new char[mainManager.getDifficulty()]).replace('\0', '0');
		HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>(); //a temporary working list of unspent transactions at a given block state.
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		//loop through blockchain to check hashes:
		for(int i=1; i < blockchain.size(); i++) {

			currentBlock = blockchain.get(i);
			previousBlock = blockchain.get(i-1);
			//compare registered hash and calculated hash:
			if(!currentBlock.hash.equals(currentBlock.calculateHash()) ){
				System.out.println("#Current Hashes not equal");
				return false;
			}
			//compare previous hash and registered previous hash
			if(!previousBlock.hash.equals(currentBlock.previousHash) ) {
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			//check if hash is solved
			if (!currentBlock.hash.substring(0, mainManager.getDifficulty()).equals(hashTarget)) {
				System.out.println("#This block hasn't been mined");
				return false;
			}

			//loop thru blockchains transactions:
			TransactionOutput tempOutput;
			for(int t=0; t <currentBlock.transactions.size(); t++) {
				Transaction currentTransaction = currentBlock.transactions.get(t);

				if(!currentTransaction.verifySignature()) {
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false;
				}
				if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false;
				}

				for (TransactionInput input : currentTransaction.inputs) {
					tempOutput = tempUTXOs.get(input.transactionOutputId);

					if(tempOutput == null) {
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}

					if(input.UTXO.value != tempOutput.value) {
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}

					tempUTXOs.remove(input.transactionOutputId);
				}

				for(TransactionOutput output: currentTransaction.outputs) {
					tempUTXOs.put(output.id, output);
				}

				if( currentTransaction.outputs.get(0).reciepient != currentTransaction.reciepient) {
					System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
					return false;
				}
				if( currentTransaction.outputs.get(1).reciepient != currentTransaction.sender) {
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}

			}

		}
		System.out.println("Blockchain is valid");
		return true;
	}

	public static void addBlock(Block newBlock) {
		newBlock.mineBlock(mainManager.getDifficulty());
		blockchain.add(newBlock);
	}
}
