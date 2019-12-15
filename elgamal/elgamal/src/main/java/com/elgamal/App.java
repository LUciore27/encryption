package com.elgamal;

import java.math.BigInteger;
import java.util.Random;
import java.util.Arrays;
import java.lang.Math;

/**
 * Hello world!
 */
public final class App {
    static Primitive pr;
    static ElgamalDecryption dec;
    static ElgamalEncryption enc;
    static BigInteger key[] = new BigInteger[4];
    static StringTrans encode;
    static StringTrans decode;
    static BigInteger[][] encodeArray;
    static BigInteger decodeArray;
    static String message;

    private App() {

    }

    /**
     * Says hello to the world.
     * 
     * @param args The arguments of the program.
     */
    public static void main(final String[] args) {
        // settings
        String sampletext = "abcdeあいう三嶋";
        encode = new StringTrans(sampletext);
        pr = new Primitive();
        key = pr.generatePrimitive();// 鍵を生成
        System.out.println(sampletext);
        System.out.println(Arrays.asList(key));

        String omega = "abcdefg";
        int epsilon = (int) omega.charAt(0);

        // encryption
        enc = new ElgamalEncryption(key, encode.stringToBigInteger());
        encodeArray = enc.encription();

        // decryption
        dec = new ElgamalDecryption(encodeArray, key[3], key[0]);
        decodeArray = dec.decryption();

        // BigInteger → String
        decode = new StringTrans(decodeArray);
        message = decode.BigIntegerToString();
        System.out.println(message);

    }
}

// 秘密鍵と公開鍵を生成
class Primitive {
    static int bitlng = 512;

    public Primitive() {
    }

    BigInteger[] generatePrimitive() {
        BigInteger[] data = new BigInteger[4];
        while (true) {
            BigInteger primitiveNom;// 求める原始元
            final Random rnd = new Random(System.currentTimeMillis());
            final BigInteger bInt = BigInteger.probablePrime(bitlng, rnd);// q
            final BigInteger p = (bInt.multiply(BigInteger.valueOf(2))).add(BigInteger.valueOf(1));// p = 2q +1
            if (bInt.isProbablePrime(100)) {
                if (p.isProbablePrime(100)) {
                    while (true) {
                        boolean check1 = true;
                        boolean check2 = true;
                        primitiveNom = BigInteger.probablePrime(bitlng, new Random(System.currentTimeMillis()));
                        if (primitiveNom.compareTo(p.subtract(BigInteger.ONE)) < 0) {
                            final BigInteger r1 = primitiveNom.modPow(BigInteger.valueOf(2), p);
                            final BigInteger r2 = primitiveNom.modPow(bInt, p);
                            if (r1.equals(BigInteger.ONE)) {
                                check1 = false;
                            }
                            if (r2.equals(BigInteger.ONE)) {
                                check2 = false;
                            }
                            if (check1 && check2) {
                                break;
                            }
                        }
                    }
                    data[0] = p;
                    data[1] = primitiveNom;
                    while (true) {
                        BigInteger x = BigInteger.probablePrime(bitlng, new Random(System.currentTimeMillis()));
                        if (x.compareTo(p.subtract(BigInteger.ONE)) < 0) {
                            data[2] = primitiveNom.modPow(x, p);
                            data[3] = x;
                            break;
                        }
                    }
                    return data;// data{p,g,y,x}
                }
            }
        }
    }
}

// 公開鍵から暗号化
class ElgamalEncryption {
    static BigInteger p;
    static BigInteger g;
    static BigInteger y;
    static int lng;
    static BigInteger message;

    ElgamalEncryption(BigInteger[] key, BigInteger message) {
        p = key[0];
        g = key[1];
        y = key[2];
        this.message = message;
    }

    BigInteger[][] encription() {
        BigInteger[][] encryptMessage = new BigInteger[2][];
        BigInteger[] c1 = new BigInteger[1];
        BigInteger[] c2 = new BigInteger[1];
        Random rnd = new Random(System.currentTimeMillis());
        while (true) {
            BigInteger r = BigInteger.probablePrime(4, rnd);
            if (r.compareTo(p) < 0) {
                c1[0] = g.modPow(r, p);
                c2[0] = y.modPow(r, p).multiply(message).mod(p);
                break;
            }
        }
        encryptMessage[0] = c1;
        encryptMessage[1] = c2;
        return encryptMessage;
    }
}

// 秘密鍵と暗号文から復号化
class ElgamalDecryption {

    static BigInteger x;
    static BigInteger p;
    static BigInteger decryptionMesssage[][];
    static BigInteger message;
    static int lng;

    ElgamalDecryption(BigInteger[][] message, BigInteger x, BigInteger p) {
        this.decryptionMesssage = message;
        this.x = x;
        this.p = p;
        this.lng = decryptionMesssage[0].length;
    }

    BigInteger decryption() {
        message = ((decryptionMesssage[0][0].modPow(p.subtract(x).subtract(BigInteger.ONE), p))
                .multiply(decryptionMesssage[1][0])).mod(p);
        return message;
    }
}

// 文字列⇔byte配列の変換
class StringTrans {

    static BigInteger decMessage = BigInteger.ZERO;
    static String encMessage;

    // decryption
    StringTrans(BigInteger message) {
        this.decMessage = message;
    }

    // encryption
    StringTrans(String message) {
        this.encMessage = message;
    }

    BigInteger stringToBigInteger() {
        byte[] asciiCode;
        int integerString = 0;
        BigInteger tmp;
        BigInteger tmp2;

        for (int i = 0; i < encMessage.length(); i++) {
            tmp = BigInteger.valueOf((int) encMessage.charAt(i));
            decMessage = decMessage.add(tmp.multiply(BigInteger.valueOf(100000).pow(i)));
        }
        decMessage = decMessage.add(BigInteger.valueOf(100000).pow(encMessage.length()));
        return decMessage;
    }

    String BigIntegerToString() {
        int bigintString = 0;
        char[] charArray;
        int lng;
        BigInteger tmp2 = BigInteger.ZERO;
        int cnt = 0;
        int j = 0;
        BigInteger checkLengeth = decMessage;

        while (true) {
            tmp2 = checkLengeth.mod(BigInteger.valueOf(100000).pow(j + 1));
            checkLengeth = checkLengeth.subtract(tmp2);
            if (tmp2.equals(BigInteger.ZERO)) {
                lng = cnt - 1;
                break;
            }
            cnt++;
            j++;
        }

        charArray = new char[lng];
        for (int i = 0; i < lng; i++) {
            tmp2 = decMessage.mod(BigInteger.valueOf(100000).pow(i + 1));
            charArray[i] = (char) tmp2.divide(BigInteger.valueOf(100000).pow(i)).intValue();
            decMessage = decMessage.subtract(tmp2);
        }

        encMessage = new String(charArray);

        return encMessage;
    }

}
