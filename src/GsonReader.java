import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.security.PublicKey;
import java.util.ArrayList;

public class GsonReader {

    public static Block readJson(String filename) {
        try {
            Gson gson = new Gson();

            BufferedReader br = new BufferedReader(new FileReader(filename));

            BlockSample block = gson.fromJson(br, BlockSample.class);
            return convertSampleToRealClass(block);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Block convertSampleToRealClass(BlockSample blockSample) {
        Block newBlock = new Block(blockSample.prevHash);
        newBlock.setHash(blockSample.hash);
        newBlock.setMerkleRoot(blockSample.merkle_root);
        newBlock.setNonce(blockSample.nonce);
        newBlock.setTimeStamp(blockSample.time_stamp);

        ArrayList<Transaction> newTransaction = new ArrayList<>();
        for (TransactionSample transactionSample : blockSample.transactions) {
            PublicKey senderPUK = StringUtil.convertStringToPUK(transactionSample.sender_public_key);
            PublicKey receiverPUK = StringUtil.convertStringToPUK(transactionSample.receiver_public_key);
            ArrayList<TransactionInput> newTransactionInput = new ArrayList<>();
            for (TransactionInputSample transactionInputSample : transactionSample.input) {
                TransactionInput newInput = new TransactionInput(transactionInputSample.transactionOutputId);
                TransactionOutputSample transOutputSample = transactionInputSample.utxo;
                TransactionOutput newtransOutput = new TransactionOutput(StringUtil.convertStringToPUK(transOutputSample.recipient),
                        transOutputSample.value, transOutputSample.parentTransactionId);
                newtransOutput.id = transOutputSample.id;
                newInput.UTXO = newtransOutput;
                newTransactionInput.add(newInput);
            }

            Transaction transaction = new Transaction(senderPUK, receiverPUK, transactionSample.value, newTransactionInput);
            ArrayList<TransactionOutput> newTransactionOutput = new ArrayList<>();
            for (TransactionOutputSample transactionOutputSample : transactionSample.output) {
                TransactionOutput transactionOutput = new TransactionOutput(StringUtil.convertStringToPUK(transactionOutputSample.recipient)
                        , transactionOutputSample.value, transactionOutputSample.parentTransactionId);
                transactionOutput.id = transactionOutputSample.id;
                newTransactionOutput.add(transactionOutput);
            }
            transaction.outputs = newTransactionOutput;
            transaction.sign = transactionSample.signature.getBytes();
            transaction.transactionId = transactionSample.id;
            newTransaction.add(transaction);
        }
        newBlock.setTransactions(newTransaction);
        return newBlock;
    }
}

class BlockSample {
    String hash;
    String prevHash;
    String merkle_root;
    long time_stamp;
    int nonce;
    int height;
    int number_of_transactions;
    ArrayList<TransactionSample> transactions;

}

class TransactionSample {
    String id; //Contains a hash of transaction*
    String sender_public_key; //Senders address/public key.
    String receiver_public_key; //Recipients address/public key.
    float value; //Contains the amount we wish to send to the recipient.
    String signature;
    ArrayList<TransactionInputSample> input;
    ArrayList<TransactionOutputSample> output;
}

class TransactionInputSample {
    String transactionOutputId;
    TransactionOutputSample utxo;

}

class TransactionOutputSample {
    String id;
    String recipient; //also known as the new owner of these coins.
    float value; //the amount of coins they own
    String parentTransactionId; //the id of the transaction this output was created in

}