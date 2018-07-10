import com.google.gson.GsonBuilder;

import javax.crypto.Cipher;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class StringUtil {

	public static KeyPair getKeyPair() throws Exception {
//        return generateKeyPair();
		return LoadKeyPair();
	}

	public static KeyPair LoadKeyPair() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		String path = "";

		// Read Public Key.
		File filePublicKey = new File(path + "public.key");
		FileInputStream fis = new FileInputStream(path + "public.key");
		byte[] encodedPublicKey = new byte[(int) filePublicKey.length()];
		fis.read(encodedPublicKey);
		fis.close();

		// Read Private Key.
		File filePrivateKey = new File(path + "private.key");
		fis = new FileInputStream(path + "private.key");
		byte[] encodedPrivateKey = new byte[(int) filePrivateKey.length()];
		fis.read(encodedPrivateKey);
		fis.close();

		// Generate KeyPair.
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(
				encodedPublicKey);
		PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

		PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(
				encodedPrivateKey);
		PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

		return new KeyPair(publicKey, privateKey);
	}

	public static void saveKeyPair(KeyPair keyPair) throws IOException {
		String path = "D:\\Data and network security\\project\\blockchain-project\\source\\";

		PrivateKey privateKey = keyPair.getPrivate();
		PublicKey publicKey = keyPair.getPublic();

		// Store Public Key.
		X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(
				publicKey.getEncoded());
		FileOutputStream fos = new FileOutputStream(path + "public.key");
		fos.write(x509EncodedKeySpec.getEncoded());
		fos.close();

		// Store Private Key.
		PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(
				privateKey.getEncoded());
		fos = new FileOutputStream(path + "private.key");
		fos.write(pkcs8EncodedKeySpec.getEncoded());
		fos.close();
	}

	public static KeyPair generateKeyPair() throws Exception {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		generator.initialize(1024, new SecureRandom());
		KeyPair keyPair = generator.generateKeyPair();

		saveKeyPair(keyPair);

		return keyPair;
	}

	//Applies Sha256 to a string and returns the result.
	public static String applySha256(String input){

		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-512");

			//Applies sha256 to our input,
			byte[] hash = digest.digest(input.getBytes("UTF-8"));

			StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Applies ECDSA Signature and returns the result ( as bytes ).
	public static byte[] applyECDSASig(PrivateKey privateKey, String input) {
		Signature dsa;
		byte[] output = new byte[0];
		try {
			dsa = Signature.getInstance("SHA256withRSA");
			dsa.initSign(privateKey);
			byte[] strByte = input.getBytes("UTF-8");
			dsa.update(strByte);
			byte[] realSig = dsa.sign();
			output = realSig;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return output;
	}

	//Verifies a String signature
	public static boolean verifyECDSASig(PublicKey publicKey, String data, byte[] signature) {
		try {
			Signature ecdsaVerify = Signature.getInstance("SHA256withRSA");
			ecdsaVerify.initVerify(publicKey);
			ecdsaVerify.update(data.getBytes("UTF-8"));
			return ecdsaVerify.verify(signature);
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	//Short hand helper to turn Object into a json string
	public static String getJson(Object o) {
		return new GsonBuilder().setPrettyPrinting().create().toJson(o);
	}

	//Returns difficulty string target, to compare to hash. eg difficulty of 5 will return "00000"
	public static String getDificultyString(int difficulty) {
		return new String(new char[difficulty]).replace('\0', '0');
	}

	public static String getStringFromKey(Key key) {
		return new String(Base64.getEncoder().encode(key.getEncoded()), StandardCharsets.UTF_8);
	}

	public static String getMerkleRoot(ArrayList<Transaction> transactions) {
		int count = transactions.size();

		List<String> previousTreeLayer = new ArrayList<String>();
		for(Transaction transaction : transactions) {
			previousTreeLayer.add(transaction.transactionId);
		}
		List<String> treeLayer = previousTreeLayer;

		while (count > 1) {
			treeLayer = new ArrayList<String>();
			for (int i = 1; i < previousTreeLayer.size(); i += 2) {
				treeLayer.add(applySha256(previousTreeLayer.get(i - 1) + previousTreeLayer.get(i)));
			}
			count = treeLayer.size();
			previousTreeLayer = treeLayer;
		}

		String merkleRoot = (treeLayer.size() == 1) ? treeLayer.get(0) : "";
		return merkleRoot;
	}

	public static PublicKey convertStringToPUK(String str) {
		PublicKey pubKey = null;
		try {
			byte[] publicKeyBytes = str.getBytes(Charset.forName("UTF-8"));
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBytes));
			pubKey = (RSAPublicKey) keyFactory.generatePublic(keySpecX509);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return pubKey;
	}

	public static PrivateKey convertStringToPRK(String str) {
		PrivateKey privateKey = null;
		try {
			byte[] privateKeyBytes = str.getBytes(StandardCharsets.UTF_8);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBytes));
			privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return privateKey;
	}

	public static String convertPUKtoString(PublicKey puk) {
		byte[] publicKeyBytes = puk.getEncoded();
		return new String(publicKeyBytes);
	}

	public static String encrypt(String plainText, PublicKey publicKey) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("RSA");
		encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

		byte[] cipherText = encryptCipher.doFinal(plainText.getBytes("UTF-8"));

		return Base64.getEncoder().encodeToString(cipherText);
	}

	public static String decrypt(String cipherText, PrivateKey privateKey) throws Exception {
		byte[] bytes = Base64.getDecoder().decode(cipherText);

		Cipher decriptCipher = Cipher.getInstance("RSA");
		decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);

		return new String(decriptCipher.doFinal(bytes), "UTF-8");
	}
}
