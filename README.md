# SFT Client Wrapper
A very Simple JAVA Wrapper for SFTP Client

### Below code should sufficied to copy the files from SFTP Server
```java
import com.springml.sftp.client.SFTPClient;

SFTPClient sftpClient = new SFTPClient(pemFile, username, null, host);
sftpClient.copy(sourcePath, targetPath);

```
