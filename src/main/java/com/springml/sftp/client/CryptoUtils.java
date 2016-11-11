package com.springml.sftp.client;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.Key;

/**
 * Created by sam on 11/11/16.
 */
public class CryptoUtils {
    private String algorithm;
    private String key;

    public CryptoUtils(String key, String algorithm) {
        this.key = key;
        this.algorithm = algorithm;
    }

    public void encrypt(File inputFile, File outputFile)
            throws Exception {
        doCrypto(Cipher.ENCRYPT_MODE, inputFile, outputFile);
    }

    public void decrypt(File inputFile, File outputFile)
            throws Exception {
        doCrypto(Cipher.DECRYPT_MODE, inputFile, outputFile);
    }

    private void doCrypto(int cipherMode, File inputFile,
                                 File outputFile) throws Exception {

        try (FileInputStream inputStream = new FileInputStream(inputFile);
                FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            Key secretKey = new SecretKeySpec(key.getBytes(), algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(cipherMode, secretKey);

            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        }
    }
}
