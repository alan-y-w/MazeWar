import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

public class MD5Test {

    public static void main(String[] args){
        
        //String word = "d00rst0p"; // 1d6571ec4951c984b78784b36b10c03e
        String word = "5ub51d3d"; // 643a5688af1e7aeab0bb036b64b2a0b0

        String hash = getHash(word);
        System.out.println(hash); 
        
//        word = "c463be62fd5252c7568d7bafd3cc4a55";
//        hash = getHash(word);
//        System.out.println(hash); 
    }

    public static String getHash(String word) {

        String hash = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BigInteger hashint = new BigInteger(1, md5.digest(word.getBytes()));
            hash = hashint.toString(16);
            while (hash.length() < 32) hash = "0" + hash;
        } catch (NoSuchAlgorithmException nsae) {
            // ignore
        }
        return hash;
    }
}
