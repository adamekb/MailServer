import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


public class Hash {
	
	private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
	        throws NoSuchAlgorithmException, InvalidKeySpecException {
		
	        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
	        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
	        Arrays.fill(password, '\u0000');
	        
	        return skf.generateSecret(spec).getEncoded();
	    }
    
    public static boolean validatePassword(char[] password, String correctHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
    	
            String[] params = correctHash.split(":");
            int iterations = Integer.parseInt(params[0]);
            byte[] salt = fromHex(params[1]);
            byte[] hash = fromHex(params[2]);
            byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
            Arrays.fill(password, '\u0000');
            
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
