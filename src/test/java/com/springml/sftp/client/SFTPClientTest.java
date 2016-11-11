package com.springml.sftp.client;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for simple App.
 */
public class SFTPClientTest {
    private static final String IDENTITY = null;
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String HOST = "ftp.ftp.com";
    private static final int PORT = 22;
    private static final String KEY = "0123456789abcdef";
    private static final String ALGORITHM = "AES";

    private SFTPClient sftpClient;
    private SFTPClient cryptoSftpClient;

    @Before
    public void setup() {
        sftpClient = new SFTPClient(IDENTITY, USERNAME, PASSWORD, HOST);
        cryptoSftpClient = new SFTPClient(IDENTITY, USERNAME, PASSWORD, HOST, PORT, true, KEY, ALGORITHM);
    }

    @Test
    public void copyTest() throws Exception {
        String copiedFilePath = sftpClient.copy("/home/ec2-user/work/files/csv/", "/home/sam/tmp/s");
        assertEquals("/home/sam/tmp/s", copiedFilePath);
    }

    @Test
    public void copyLatestTest() throws Exception {
        String copiedFilePath = sftpClient.copyLatest("/home/ec2-user/work/files/csv/sample*", "/home/sam/tmp/s");
        assertEquals("/home/sam/tmp/s/sample2.csv", copiedFilePath);
    }

    @Test
    public void copyFileToLocalDirTest() throws Exception {
        sftpClient.copy("/home/ec2-user/work/files/csv/sample.csv", "/home/sam/tmp/s");
    }

    @Test
    public void copyFileToLocalFileTest() throws Exception {
        sftpClient.copy("/home/ec2-user/work/files/csv/sample.csv", "/home/sam/tmp/sample.csv");
    }


    @Test
    public void copyToFTPTest() throws Exception {
        String copiedFilePath = sftpClient.copyToFTP("/home/sam/tmp/s", "/home/ec2-user/work/files/csv/output");
        assertEquals("/home/ec2-user/work/files/csv/output", copiedFilePath);
    }

    @Test
    public void copyLatestToFTPTest() throws Exception {
        sftpClient.copyLatestToFTP("/home/sam/tmp/s/sample*", "/home/ec2-user/work/files/csv/output");
    }

    @Test
    public void copyFileToFTPDirTest() throws Exception {
        sftpClient.copyToFTP("/home/sam/tmp/sample.csv", "/home/ec2-user/work/files/csv/output/");
    }

    @Test
    public void copyFileToFTPFileTest() throws Exception {
        sftpClient.copyToFTP("/home/sam/tmp/sample.csv", "/home/ec2-user/work/files/csv/output/sample.csv");
    }

    @Test
    public void encryptAndCopyToFTPTest() throws Exception {
        String copiedFilePath = cryptoSftpClient.copyToFTP("/home/sam/tmp/s", "/temptest/misc/unittests/output");
        assertEquals("/temptest/misc/unittests/output", copiedFilePath);
        cryptoSftpClient.copy("/temptest/misc/unittests/output", "/home/sam/tmp/s/decryptFile");
    }

    @Test
    public void encryptAndCopyFileToFTPFileTest() throws Exception {
        cryptoSftpClient.copyToFTP("/home/sam/tmp/sample.csv", "/temptest/misc/unittests/output/sample.csv");
        cryptoSftpClient.copy("/temptest/misc/unittests/output/sample.csv", "/home/sam/tmp/sample_decrypted.csv");
    }

}
