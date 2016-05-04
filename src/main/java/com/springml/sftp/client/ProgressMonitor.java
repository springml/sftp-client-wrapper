package com.springml.sftp.client;

import java.util.logging.Logger;

import com.jcraft.jsch.SftpProgressMonitor;

public class ProgressMonitor implements SftpProgressMonitor {
    private static final Logger LOG = Logger.getLogger(ProgressMonitor.class.getName());

    public void init(int op, String src, String dest, long max) {
        LOG.info("Transfering file of size " + max + " Bytes");
    }

    public boolean count(long count) {
        LOG.info("Transfered " + count + " Bytes ...");
        return true;
    }

    public void end() {
        LOG.info("Transfer Complete");
    }

}
