package com.springml.sftp.client;

import java.util.Vector;
import java.util.logging.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SFTPClient {
    private static final Logger LOG = Logger.getLogger(SFTPClient.class.getName());

    private static final String STR_STRICT_HOST_KEY_CHECKING = "StrictHostKeyChecking";
    private static final String STR_SFTP = "sftp";
    private static final String STR_NO = "no";

    private String identity;
    private String username;
    private String password;
    private String host;
    private int port;

    public SFTPClient(String identity, String username, String password, String host) {
        this(identity, username, password, host, 22);
    }

    public SFTPClient(String identity, String username, String password, String host, int port) {
        this.identity = identity;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public void copy(String source, String target) throws Exception {
        ChannelSftp sftpChannel = createSFTPChannel();
        copyInternal(sftpChannel, source, target);
        releaseConnection(sftpChannel);
        LOG.info("Copied files successfully...");
    }

    private void copyInternal(ChannelSftp sftpChannel, String source, String target) throws Exception {
        LOG.info("Copying files from " + source + " to " + target);
        try {
            sftpChannel.cd(source);
            copyDir(sftpChannel, source, target);
        } catch (Exception e) {
            // Source is a file
            sftpChannel.get(source, target);
        }
    }

    private void copyDir(ChannelSftp sftpChannel, String source, String target) throws Exception {
        LOG.info("Copying files from " + source + " to " + target);

        sftpChannel.cd(source);
        sftpChannel.lcd(target);

        Vector<ChannelSftp.LsEntry> childFiles = sftpChannel.ls(".");
        for (LsEntry lsEntry : childFiles) {
            String entryName = lsEntry.getFilename();
            LOG.fine("File Entry " + entryName);

            if (!entryName.equals(".") && !entryName.equals("..")) {
                if (lsEntry.getAttrs().isDir()) {
                    copyInternal(sftpChannel, source + entryName + "/", target);
                } else {
                    LOG.info("Copying file " + entryName);
                    sftpChannel.get(entryName, entryName);
                }
            }
        }
    }

    private ChannelSftp createSFTPChannel() throws Exception {
        JSch jsch = new JSch();
        boolean useIdentity = identity != null && !identity.isEmpty();
        if (useIdentity) {
            jsch.addIdentity(identity);
        }

        Session session = jsch.getSession(username, host, port);
        session.setConfig(STR_STRICT_HOST_KEY_CHECKING, STR_NO);
        if (!useIdentity) {
            session.setPassword(password);
        }
        session.connect();

        Channel channel = session.openChannel(STR_SFTP);
        channel.connect();

        return (ChannelSftp) channel;
    }

    private void releaseConnection(ChannelSftp sftpChannel) throws Exception {
        Session session = sftpChannel.getSession();
        sftpChannel.exit();
        session.disconnect();
    }
}
