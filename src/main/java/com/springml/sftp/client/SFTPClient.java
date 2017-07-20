package com.springml.sftp.client;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Logger;

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
    private CryptoUtils cryptoUtils;
    private boolean runCrypto;
    private String passPhrase;

    public SFTPClient(String identity, String username, String password, String host) {
        this(identity, username, password, host, 22);
    }

    public SFTPClient(String identity, String username, String password, String host, int port) {
        this(identity, null, username, password, host, port, false, null);
    }

    public SFTPClient(String identity, String passPhrase, String username,
                      String password, String host, int port) {
        this(identity, passPhrase, username, password, host, port, false, null);
    }

    public SFTPClient(String identity, String passPhrase, String username, String password,
                      String host, boolean runCrypto, String secretKey) {
        this(identity, passPhrase, username, password, host, 22, runCrypto, secretKey);
    }

    public SFTPClient(String identity, String passPhrase, String username,
                      String password, String host, int port,
                      boolean runCrypto, String secretKey) {
        this(identity, passPhrase, username, password, host, port, runCrypto, secretKey, "AES");
    }


    public SFTPClient(String identity, String passPhrase, String username, String password,
                      String host, int port, boolean runCrypto,
                      String secretKey, String algorithm) {
        this.identity = identity;
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
        this.runCrypto = runCrypto;
        this.passPhrase = passPhrase;
        if (runCrypto) {
            this.cryptoUtils = new CryptoUtils(secretKey, algorithm);
        }
    }

    public String copy(String source, String target) throws Exception {
        ChannelSftp sftpChannel = createSFTPChannel();
        copyInternal(sftpChannel, source, target);
        releaseConnection(sftpChannel);
        LOG.info("Copied files successfully...");

        return target;
    }

    public String copyLatest(String source, String target) throws Exception {
        ChannelSftp sftpChannel = createSFTPChannel();
        String latestSource = getLatestSource(sftpChannel, source);
        copyInternal(sftpChannel, latestSource, target);
        releaseConnection(sftpChannel);
        LOG.info("Copied files successfully...");

        return getCopiedFilePath(latestSource, target);
    }

    public String copyLatestToFTP(String source, String target) throws Exception {
        ChannelSftp sftpChannel = createSFTPChannel();
        String latestSource = getLatestLocalSource(source);
        copyInternalToFTP(sftpChannel, latestSource, target);
        releaseConnection(sftpChannel);
        LOG.info("Copied files successfully...");

        return getCopiedFilePath(latestSource, target);
    }

    public String copyToFTP(String source, String target) throws Exception {
        ChannelSftp sftpChannel = createSFTPChannel();
        copyInternalToFTP(sftpChannel, source, target);
        releaseConnection(sftpChannel);
        LOG.info("Copied files successfully...");

        return target;
    }

    private String getCopiedFilePath(String latestSource, String target) {
        String copiedFileName = FilenameUtils.getName(latestSource);
        return FilenameUtils.concat(target, copiedFileName);
    }

    private String getLatestSource(ChannelSftp sftpChannel, String source) throws Exception {
        Vector ls = sftpChannel.ls(source);

        String basePath = FilenameUtils.getPath(source);
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        LOG.fine("Base Path : " + basePath);
        int latestModTime = 0;
        String fileName = FilenameUtils.getBaseName(source);
        for (int i = 0, size = ls.size(); i < size; i++) {
            LsEntry entry = (LsEntry) ls.get(i);
            int modTime = entry.getAttrs().getMTime();
            if (latestModTime < modTime) {
                latestModTime = modTime;
                fileName = entry.getFilename();
            }
        }

        return FilenameUtils.concat(basePath, fileName);
    }

    private String getLatestLocalSource(String source) throws Exception {
        String fileName = FilenameUtils.getBaseName(source);
        String basePath = FilenameUtils.getPath(source);
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }

        File baseDir = new File(basePath);
        File[] filteredFiles = baseDir.listFiles(new FileNameFilter(fileName));

        LOG.fine("Base Path : " + basePath);
        long latestModTime = 0;
        for (int i = 0; i < filteredFiles.length; i++) {
            long modTime = filteredFiles[i].lastModified();
            if (latestModTime < modTime) {
                latestModTime = modTime;
                fileName = filteredFiles[i].getName();
            }
        }

        return FilenameUtils.concat(basePath, fileName);
    }

    private void copyInternal(ChannelSftp sftpChannel, String source, String target) throws Exception {
        LOG.info("Copying file from " + source + " to " + target);
        try {
            sftpChannel.cd(source);
            copyDir(sftpChannel, source, target);
        } catch (Exception e) {
            // Source is a file
            sftpChannel.get(source, target);
            decrypt(target);
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
                    sftpChannel.get(entryName, entryName, new ProgressMonitor());
                    decrypt(target + File.separator + entryName);
                }
            }
        }
    }

    private void decrypt(String fileLocation) throws Exception {
        if (runCrypto) {
            LOG.info("Decrypting " + fileLocation);
            String tempFileLocation = fileLocation + ".temp";
            File tempFile = new File(tempFileLocation);
            File actualFile = new File(fileLocation);
            FileUtils.moveFile(actualFile, tempFile);

            cryptoUtils.decrypt(tempFile, actualFile);

            FileUtils.deleteQuietly(tempFile);
        }
    }

    private void copyInternalToFTP(ChannelSftp sftpChannel, String source, String target) throws Exception {
        LOG.info("Copying files from " + source + " to " + target);
        try {
            sftpChannel.lcd(source);
            copyDirToFTP(sftpChannel, source, target);
        } catch (Exception e) {
            // Source is a file
            encrypt(source);
            sftpChannel.put(source, target);
        }
    }

    private void copyDirToFTP(ChannelSftp sftpChannel, String source, String target) throws Exception {
        LOG.info("Copying files from " + source + " to " + target);

        sftpChannel.lcd(source);
        sftpChannel.cd(target);

        Collection<File> childFiles = FileUtils.listFiles(new File(source), null, false);
        for (File file : childFiles) {
            String entryName = file.getName();
            LOG.fine("File Entry " + entryName);

            if (!entryName.equals(".") && !entryName.equals("..")) {
                if (file.isDirectory()) {
                    copyInternalToFTP(sftpChannel, source + entryName + "/", target);
                } else {
                    LOG.info("Copying file " + entryName);
                    encrypt(source + File.separator + entryName);
                    sftpChannel.put(entryName, entryName, new ProgressMonitor());
                }
            }
        }
    }

    private void encrypt(String fileLocation) throws Exception {
        if (runCrypto) {
            LOG.info("Encrypting " + fileLocation);
            String tempFileLocation = fileLocation + ".temp";
            File tempFile = new File(tempFileLocation);
            File actualFile = new File(fileLocation);
            FileUtils.moveFile(actualFile, tempFile);

            cryptoUtils.encrypt(tempFile, actualFile);

            FileUtils.deleteQuietly(tempFile);
        }
    }

    private ChannelSftp createSFTPChannel() throws Exception {
        JSch jsch = new JSch();
        boolean useIdentity = identity != null && !identity.isEmpty();
        if (useIdentity) {
            if (passPhrase != null) {
                jsch.addIdentity(identity, passPhrase);
            } else {
                jsch.addIdentity(identity);
            }
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
