import org.postgresql.util.PSQLException;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {

	public PrivateKey privateKey;
	public PublicKey publicKey;
	public static int mainId = engine.getLastWalletID();
	private int id;

	public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

	public Wallet() throws PSQLException {
		this.id = ++mainId;
		generateKeyPair();
		saveDB();
	}

	public Wallet(int id, String publicKeyS, String privateKeyS) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
		this.id = id;

//        this.publicKey.getEncoded() = publicKeyS.getBytes();
//        this.privateKey.getEncoded() = privateKeyS.getBytes();
//
//        KeyFactory keyFactory = KeyFactory.getInstance("ECDSA");
//        EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyS.getBytes());
//        PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);
//
//        EncodedKeySpec publicKeySpec = new PKCS8EncodedKeySpec(publicKeyS.getBytes());
//        PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);
//        KeyPair keyPair = new KeyPair(publicKey, privateKey);
//
//        this.privateKey = keyPair.getPrivate();
//        this.publicKey = keyPair.getPublic();
	}

	public void saveDB() throws PSQLException {
		String save = "insert into wallet values ('" + id + "','" + privateKey.getEncoded() +
				"','" + publicKey.getEncoded() + "');";
		engine.p1.getTable(save);
	}


	public void generateKeyPair() {
		try {
			Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA","BC");
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
			ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
			// Initialize the key generator and generate a KeyPair
			keyGen.initialize(ecSpec, random); //256 
			KeyPair keyPair = keyGen.generateKeyPair();
			// Set the public and private keys from the keyPair
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();

		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int getId() {
		return id;
	}

	public float getBalance() {
		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: NoobChain.UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			if(UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
				UTXOs.put(UTXO.id,UTXO); //add it to our list of unspent transactions.
				total += UTXO.value ;
			}
		}
		return total;
	}

	public Transaction sendFunds(PublicKey _recipient,float value ) {
		if(getBalance() < value) {
			System.out.println("#Not Enough funds to send transaction. Transaction Discarded.");
			return null;
		}
		ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

		float total = 0;
		for (Map.Entry<String, TransactionOutput> item: UTXOs.entrySet()){
			TransactionOutput UTXO = item.getValue();
			total += UTXO.value;
			inputs.add(new TransactionInput(UTXO.id));
			if(total > value) break;
		}

		Transaction newTransaction = new Transaction(publicKey, _recipient , value, inputs);
		newTransaction.generateSignature(privateKey);

		for(TransactionInput input: inputs){
			UTXOs.remove(input.transactionOutputId);
		}

		return newTransaction;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}
}


