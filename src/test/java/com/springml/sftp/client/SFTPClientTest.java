package com.springml.sftp.client;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class SFTPClientTest {
    private SFTPClient sftpClient;

    @Before
    public void setup() {
        sftpClient = new SFTPClient("pem", "user", "password", "host");
    }

    @Test
    public void copyTest() throws Exception {
        sftpClient.copy("/home/ec2-user/work/files/csv", "/home/sam/tmp/s");
    }

    @Test
    public void copyLatestTest() throws Exception {
        sftpClient.copyLatest("/home/ec2-user/work/files/csv/sample*", "/home/sam/tmp/s");
    }

    @Test
    public void copyFileToLocalDirTest() throws Exception {
        sftpClient.copy("/home/ec2-user/work/files/csv/sample.csv", "/home/sam/tmp/s");
    }

    @Test
    public void copyFileToLocalFileTest() throws Exception {
        sftpClient.copy("/home/ec2-user/work/files/csv/sample.csv", "/home/sam/tmp/sample.csv");
    }

}
