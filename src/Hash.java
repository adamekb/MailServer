import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class Hash {

	public static String createHash(String password)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		char[] pwdArray = password.toCharArray();

		SecureRandom random = new SecureRandom();
		byte[] salt = new byte[24];
		random.nextBytes(salt);

		byte[] hash = pbkdf2(pwdArray, salt, 1000, 24);

		return 1000 + ":" + toHex(salt) + ":" +  toHex(hash);
	}
	
	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
	        throws NoSuchAlgorithmException, InvalidKeySpecException {
		
	        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
	        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        return skf.generateSecret(spec).getEncoded();
	    }
	
    private static String toHex(byte[] array) {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0) {
        	return String.format("%0" + paddingLength + "d", 0) + hex;
        } else {
        	return hex;
        }
            
    }
    
    public static boolean validatePassword(String password, String correctHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
    	
    		char[] pwdArray = password.toCharArray();
    	
            String[] params = correctHash.split(":");
            int iterations = Integer.parseInt(params[0]);
            byte[] salt = fromHex(params[1]);
            byte[] hash = fromHex(params[2]);
            byte[] testHash = pbkdf2(pwdArray, salt, iterations, hash.length);

            return slowEquals(hash, testHash);
        }
    
    private static byte[] fromHex(String hex) {
    	
        byte[] binary = new byte[hex.length() / 2];
        for(int i = 0; i < binary.length; i++) {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }
    
    private static boolean slowEquals(byte[] a, byte[] b) {
    	
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++) {
        	diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
