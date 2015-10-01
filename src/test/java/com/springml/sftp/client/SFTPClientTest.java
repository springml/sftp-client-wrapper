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
        sftpClient = new SFTPClient("/home/sam/Downloads/sml-predictiveapps.pem", "ec2-user", null, "ec2-52-27-55-223.us-west-2.compute.amazonaws.com");
    }

    @Test
    public void copyTest() throws Exception {
        sftpClient.copy("/home/ec2-user/work/files/csv", "/home/sam/tmp/s");
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
