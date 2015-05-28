import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Base64;


public class Rsa {
	static KeyPairGenerator generator;

	public static KeyPair generateNewKeys () {
		try {
			generator = KeyPairGenerator.getInstance("RSA");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generator.generateKeyPair();
	}

	public static String encrypt (char[] msg, Key key) {
		return encrypt(msg.toString(), key);
	}
	
	public static String encrypt (String msg, Key key) {
		String encryptedString = null;
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, key); 
			encryptedString = Base64.encodeBase64String(c.doFinal(msg.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | IOException | BadPaddingException e) {
			e.printStackTrace();
		}
		return encryptedString;
	}

	public static String decrypt (String msg, Key key) {
		String decryptedString = null;
		try {
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.DECRYPT_MODE, key);
			decryptedString = new String(c.doFinal(Base64.decodeBase64(msg)));
		} catch (IllegalBlockSizeException | BadPaddingException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
		return decryptedString;
	}

	public static String keyToString (Key key) {
		return Base64.encodeBase64String(key.getEncoded());
	}

	public static Key stringToPublicKey (String string) {
		PublicKey publicKey = null;
		try {
			X509EncodedKeySpec spec = new X509EncodedKeySpec(Base64.decodeBase64(string));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			publicKey = kf.generatePublic(spec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return publicKey;
	}

	public static Key stringToPrivateKey (String string) {
		PrivateKey privateKey = null;
		try {
			PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(Base64.decodeBase64(string));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			privateKey = kf.generatePrivate(specPriv);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
		}
		return privateKey;
	}
}