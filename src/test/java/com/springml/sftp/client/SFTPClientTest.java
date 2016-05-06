package com.springml.sftp.client;

import static org.junit.Assert.*;

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
//        sftpClient = new SFTPClient("pem", "user", "password", "host");
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

}
