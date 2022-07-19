import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Random;

public class RSA {

    private static int n, p, q, e, d, z;

    private static void init() {
        p = 53;
        q = 59;
        n = p * q;
        z = (p - 1) * (q - 1);
    }

    public static int[] publicKey() {
        int[] key = { generateE(), n };
        return key;
    }

    public static int[] privateKey(int e) {
        int[] key = { generateD(e), n };
        return key;
    }

    public static int generateE() {
        init();
        Random rand = new Random();
        e = rand.nextInt(n) + 1;

        if (gcd(e, z) != 1) {
            return generateE();
        }

        return e;
    }

    public static int generateD(int e) {
        init();
        d = modInverse(e, z);
        return d;
    }

    public static int gcd(int a, int b) {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }

    public static int powerMod(int base, int exp, int mod) {
        int result = 1;
        base %= mod;

        while (exp > 0) {
            if ((exp & 1) != 0)
                result = (result * base) % mod;

            exp >>= 1;
            base = base * base % mod;
        }

        return result < 0 ? result + mod : result;
    }

    public static int modInverse(int a, int m) {
        int m0 = m;
        int y = 0, x = 1;

        if (m == 1)
            return 0;

        while (a > 1) {
            int q = a / m;
            int t = m;

            m = a % m;
            a = t;
            t = y;

            y = x - q * y;
            x = t;

            if (m == 0)
                break;
        }

        if (x < 0)
            x += m0;

        return x;
    }

    public static ArrayList<Integer> encode(String m) {
        ArrayList<Integer> result = new ArrayList<>();

        for (int i = 0; i < m.length(); i++) {
            int element = m.charAt(i);
            result.add(element);
        }

        return result;
    }

    public static String decode(ArrayList<Integer> list) {
        String result = "";

        for (int l : list) {
            byte[] bytes = ByteBuffer.allocate(8).putInt(l).array();
            String temp = new String(bytes);

            if (l != 32)
                temp = temp.trim();
            else
                temp = " ";

            result += temp;
        }

        return result;
    }

    public static String encrypt(String m, int e, int n) {
        ArrayList<Integer> encodedList = encode(m);
        String encryptedStr = "";

        for (int i : encodedList) {
            encryptedStr += new String(powerMod(i, e, n) + ",");
        }
        encryptedStr = encryptedStr.substring(0, encryptedStr.length() - 1);

        return encryptedStr;
    }

    public static String decrypt(String c, int d, int n) {
        String[] encodedCharArr = c.split(",");
        ArrayList<Integer> encodedList = new ArrayList<>();
        String decryptedString;

        for (int i = 0; i < encodedCharArr.length; i++) {
            encodedList.add(powerMod(Integer.parseInt(encodedCharArr[i]), d, n));
        }

        decryptedString = decode(encodedList);

        return decryptedString;
    }

    public static void main(String[] args) {
        int[] pubk = publicKey();
        int[] prik = privateKey(pubk[0]);
        String m = "hello world";
        String c = encrypt(m, pubk[0], pubk[1]);
        String m1 = decrypt(c, prik[0], prik[1]);
        System.out.println(m1);
    }

}
