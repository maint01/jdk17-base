package vn.com.lifesup.base.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

@Log4j2
public class FTPUtil {
    private FTPUtil() {
    }

    /**
     * Connection FTPClient
     *
     * @param server server
     * @param port   port
     * @param user   user
     * @param pass   pass
     * @throws IOException if any network or IO error occurred.
     */
    public static FTPClient connectionFTPClient(String server, int port, String user, String pass)
            throws IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        showServerReply(ftpClient);
        int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            log.info("Could not connect to the server: {}", server);
            return null;
        }
        boolean success = ftpClient.login(user, pass);
        showServerReply(ftpClient);
        if (!success) {
            log.info("Could not login to the server: {}", server);
            return null;
        }
        ftpClient.printWorkingDirectory();
        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.addProtocolCommandListener(new PrintCommandListener(
                new PrintWriter(System.out)));
        return ftpClient;
    }

    /**
     * Disconnection FTPClient
     *
     * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
     * @throws IOException if any network or IO error occurred.
     */
    public static void disconnectionFTPClient(FTPClient ftpClient) throws IOException {
        if (ftpClient.isConnected()) {
            ftpClient.disconnect();
        }
    }

    /**
     * Upload a whole directory (including its nested sub directories and files) to a FTP server.
     *
     * @param ftpClient     an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param remoteDirPath Path of the file on remote the server
     * @param fileName      File name
     * @param bytes         File bytes
     * @throws IOException if any network or IO error occurred.
     */
    public static void uploadDirectory(FTPClient ftpClient, String remoteDirPath,
                                       String fileName, byte[] bytes)
            throws IOException {
        makeDirectories(ftpClient, remoteDirPath);
        boolean uploaded = uploadSingleFile(ftpClient, remoteDirPath, fileName, bytes);
        if (uploaded) {
            System.out.println("UPLOADED a file to: " + remoteDirPath + "/" + fileName);
        } else {
            System.out.println("COULD NOT upload the file: " + fileName);
            throw new IOException();
        }
    }

    /**
     * Upload a single file to the FTP server.
     *
     * @param ftpClient     an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param remoteDirPath Path of the file on remote the server
     * @param fileName      File name
     * @param bytes         File bytes
     * @return true if the file was uploaded successfully, false otherwise
     * @throws IOException if any network or IO error occurred.
     */
    public static boolean uploadSingleFile(FTPClient ftpClient, String remoteDirPath,
                                           String fileName, byte[] bytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(bytes)){
            ftpClient.changeWorkingDirectory(remoteDirPath);
            return ftpClient.storeFile(fileName, inputStream);
        }
    }

    /**
     * Creates a nested directory structure on a FTP server
     *
     * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
     * @param dirPath   Path of the directory, i.e /projects/java/ftp/demo
     * @return true if the directory was created successfully, false otherwise
     * @throws IOException if any error occurred during client-server communication
     */
    public static boolean makeDirectories(FTPClient ftpClient, String dirPath)
            throws IOException {
        String[] pathElements = dirPath.split("/");
        if (pathElements != null && pathElements.length > 0) {
            for (String singleDir : pathElements) {
                boolean existed = ftpClient.changeWorkingDirectory(singleDir);
                showServerReply(ftpClient);
                if (!existed) {
                    boolean created = ftpClient.makeDirectory(singleDir);
                    showServerReply(ftpClient);
                    if (created) {
                        System.out.println("CREATED directory: " + singleDir);
                        ftpClient.changeWorkingDirectory(singleDir);
                    } else {
                        System.out.println("COULD NOT create directory: " + singleDir);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static void showServerReply(FTPClient ftpClient) {
        String[] replies = ftpClient.getReplyStrings();
        if (replies != null && replies.length > 0) {
            for (String aReply : replies) {
                System.out.println("SERVER: " + aReply);
            }
        }
    }

}
