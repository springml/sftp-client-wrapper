package com.springml.sftp.client;

import com.jcraft.jsch.SftpProgressMonitor;

import java.util.logging.Logger;

public class ProgressMonitor implements SftpProgressMonitor {
    private static final Logger LOG = Logger.getLogger(ProgressMonitor.class.getName());

    public void init(int op, String src, String dest, long max) {
        LOG.info("Transferring file of size " + max + " Bytes");
    }

    public boolean count(long count) {
        LOG.info("Transferred " + count + " Bytes ...");
        return true;
    }

    public void end() {
        LOG.info("Transfer Complete");
    }

}
