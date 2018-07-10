import org.postgresql.util.PSQLException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {

    static private PrivateKey privateKey;
    static private PublicKey publicKey;
    static private int mainId = engine.getLastWalletID();
    static private int id;

    public HashMap<String,TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();

    public Wallet(int id) {
        this.id = id;
        generateKeyPair();
    }

    public Wallet() throws Exception {
        id = ++mainId;
        generateKeyPair();
//        saveDB();
    }

    public Wallet(String privateKeyS, String publicKeyS) throws Exception {
        id = ++mainId;
        privateKey = StringUtil.convertStringToPRK(privateKeyS);
        publicKey = StringUtil.convertStringToPUK(publicKeyS);
        saveDB();
    }

    public Wallet(int ID, String privateKeyS, String publicKeyS) throws PSQLException, NoSuchAlgorithmException, InvalidKeySpecException {
        id = ID;
        privateKey = StringUtil.convertStringToPRK(privateKeyS);
        publicKey = StringUtil.convertStringToPUK(publicKeyS);

    }

    public static void saveDB() throws Exception {
        String save = "insert into wallet values ('" + id + "','" + StringUtil.encrypt(StringUtil.getStringFromKey(privateKey), NoobChain.keyPair.getPublic()) +
                "','" + StringUtil.encrypt(StringUtil.getStringFromKey(publicKey), NoobChain.keyPair.getPublic()) + "');";
        engine.p1.getTable(save);
    }

    public void generateKeyPair() {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            // Initialize the key generator and generate a KeyPair
            keyGen.initialize(1024); //256
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the keyPair
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();

        }catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getId() {
        return id;
    }

    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : NoobChain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) { //if output belongs to me ( if coins belong to me )
                UTXOs.put(UTXO.id, UTXO); //add it to our list of unspent transactions.
                total += UTXO.value;
            }
        }
        return total;
    }

    public Transaction sendFunds(PublicKey _recipient, float value) {
        if (getBalance() < value + NoobChain.mainManager.getTransactionFee()) {
            System.out.println("Not Enough funds to send transaction. Transaction Discarded.");
            return null;
        }
        ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        for (TransactionInput input : inputs) {
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


