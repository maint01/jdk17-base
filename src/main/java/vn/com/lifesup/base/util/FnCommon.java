package vn.com.lifesup.base.util;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import vn.com.lifesup.base.constant.ConfigConstants;
import vn.com.lifesup.base.dto.common.BaseSearchDTO;

import java.io.*;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Phuong thuc chung cho toan bo project
 */
@Log4j2
@Component
public class FnCommon {

    public static final char DEFAULT_ESCAPE_CHAR = '\\';

    private FnCommon() {
    }

    /**
     * Convert date date to string date
     *
     * @param date Date
     * @param isFullDateTime:true: full date time, false: date sort
     * @return String
     */
    public static String convertDateToString(Date date, boolean isFullDateTime) {
        String strDate;
        if (date == null) {
            return StringUtils.EMPTY;
        }
        if (isFullDateTime) {
            strDate = new SimpleDateFormat(ConfigConstants.DATETIME_FORMAT).format(date);
        } else {
            strDate = new SimpleDateFormat(ConfigConstants.DATE_FORMAT).format(date);
        }
        return strDate;
    }

    /**
     * Convert date to string without separator
     *
     * @param date
     * @param isFullDateTime:true: full date time, false: date sort
     * @return
     */
    public static String convertDateToStringWithoutSeparator(Date date, Boolean isFullDateTime) {
        String strDate;
        if (date == null) {
            return StringUtils.EMPTY;
        }
        if (isFullDateTime) {
            strDate = new SimpleDateFormat("yyyyMMdd_HHmmss").format(date);
        } else {
            strDate = new SimpleDateFormat("yyyyMMdd").format(date);
        }
        return strDate;
    }


    /**
     * Convert string date to sql date date
     *
     * @param strDate
     * @param isFullDateTime:true: full date time, false: date sort
     * @return
     */
    public static java.sql.Date convertStringToSqlDate(String strDate, Boolean isFullDateTime) {
        if (strDate == null || StringUtils.EMPTY.equals(strDate)) {
            return null;
        }
        try {
            Date date;
            if (isFullDateTime) {
                date = new SimpleDateFormat(ConfigConstants.DATETIME_FORMAT).parse(strDate);
            } else {
                date = new SimpleDateFormat(ConfigConstants.DATE_FORMAT).parse(strDate);
            }
            return new java.sql.Date(date.getTime());
        } catch (ParseException e) {
            log.error("Loi! convertStringToDate: " + e.getMessage());
        }
        return null;
    }

    /**
     * Go bo dau tieng viet
     *
     * @param s
     * @return
     */
    public static String removeAccent(String s) {
        if (s == null) {
            return StringUtils.EMPTY;
        }
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll(StringUtils.EMPTY).replace("đ", "d").replace("Đ", "D");
    }

    public static String makeSlug(String s) {
        String nowhitespace = Pattern.compile("[\\s]").matcher(s).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = Pattern.compile("[^\\w-]").matcher(normalized).replaceAll(StringUtils.EMPTY);
        return slug.toLowerCase(Locale.ENGLISH);
    }

    public static String getNumberAndDotFromString(String strInput) {
        String digits = strInput.replaceAll("[^0-9./:]", StringUtils.EMPTY);
        return digits;
    }

    /**
     * Convert byte[] to string
     *
     * @param bytes
     * @return
     */
    public static String convertByteArrayToString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Convert string to byte[]
     *
     * @param str
     * @return
     */
    public static byte[] convertStringToByteArray(String str) {
        if (str == null || StringUtils.EMPTY.equals(str.trim())) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(str);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Check string is not blank
     *
     * @param str
     * @return
     */
    public static boolean isStringNotBlank(String str) {
        return str != null && !StringUtils.EMPTY.equals(str.trim());
    }

    /**
     * Check is date
     *
     * @param strDate
     * @param isFullDateTime:true: full date time, false: date sort
     * @return
     */
    public static boolean isDate(String strDate, boolean isFullDateTime) {
        if (strDate == null || strDate.isEmpty()) {
            return false;
        }
        try {
            if (isFullDateTime) {
                if (strDate.length() == ConfigConstants.DATETIME_FORMAT.length()) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigConstants.DATETIME_FORMAT);
                    simpleDateFormat.setLenient(false);
                    simpleDateFormat.parse(strDate);
                } else {
                    return false;
                }
            } else {
                if (strDate.length() == ConfigConstants.DATE_FORMAT.length()) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ConfigConstants.DATE_FORMAT);
                    simpleDateFormat.setLenient(false);
                    simpleDateFormat.parse(strDate);
                } else {
                    return false;
                }
            }
            return true;
        } catch (ParseException e) {
            log.error("Loi! convertStringToDate: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check is not date
     *
     * @param strDate
     * @param isFullDateTime:true: full date time, false: date sort
     * @return
     */
    public static boolean isNotDate(String strDate, Boolean isFullDateTime) {
        return !isDate(strDate, isFullDateTime);
    }

    /**
     * Convert util date to sql date
     *
     * @param date
     * @return
     */
    public static java.sql.Date convertUtilDateToSqlDate(Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Date(date.getTime());
    }

    public static boolean deleteFtpFile(String server, int port, String user, String pass,
                                        String fullPath) throws IOException {
        FTPClient ftpClient = FTPUtil.connectionFTPClient(server, port, user, pass);
        boolean deleted = ftpClient.deleteFile(fullPath);
        if (deleted) {
            FTPUtil.disconnectionFTPClient(ftpClient);
            return true;
        } else {
            FTPUtil.disconnectionFTPClient(ftpClient);
            return false;
        }
    }

    public static boolean existsFtpFile(String server, int port, String user, String pass,
                                        String fullPath) throws IOException {
        FTPClient ftpClient = FTPUtil.connectionFTPClient(server, port, user, pass);
        FTPFile[] remoteFiles = ftpClient.listFiles(fullPath);
        if (remoteFiles.length > 0) {
            FTPUtil.disconnectionFTPClient(ftpClient);
            return true;
        } else {
            FTPUtil.disconnectionFTPClient(ftpClient);
            return false;
        }
    }

    public static byte[] getFtpFile(String server, int port, String user, String pass,
                                    String fullPath)
            throws IOException {
        FTPClient ftpClient = FTPUtil.connectionFTPClient(server, port, user, pass);
        InputStream inputStream = ftpClient.retrieveFileStream(fullPath);
        if (inputStream != null) {
            FTPUtil.disconnectionFTPClient(ftpClient);
            byte[] bytes = convertFileToByte(inputStream);
            return bytes;
        } else {
            System.out.println("File: " + fullPath + " not exist ! ");
            FTPUtil.disconnectionFTPClient(ftpClient);
            return null;
        }
    }

    public static String saveFtpFile(String server, int port, String user, String pass,
                                     String ftpFolder, String originalFilename, byte[] bytes) throws IOException {
        FTPClient ftpClient = FTPUtil.connectionFTPClient(server, port, user, pass);
        FTPUtil.uploadDirectory(ftpClient, ftpFolder, originalFilename, bytes);
        FTPUtil.disconnectionFTPClient(ftpClient);
        return ftpFolder + "/" + originalFilename;
    }

    public static String saveFtpFile(String server, int port, String user, String pass,
                                     String ftpFolder, String originalFilename, byte[] bytes, Date date) throws IOException {
        if (date == null) {
            date = new Date();
        }
        String fileName = createFileName(originalFilename, date);
        FTPClient ftpClient = FTPUtil.connectionFTPClient(server, port, user, pass);
        FTPUtil.uploadDirectory(ftpClient, ftpFolder + "/"
                + createPathByDate(date), fileName, bytes);
        FTPUtil.disconnectionFTPClient(ftpClient);
        return ftpFolder + "/" + createPathByDate(date) + "/" + fileName;
    }

    public static byte[] convertFileToByte(InputStream fis) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        try {
            for (int readNum; (readNum = fis.read(buf)) != -1; ) {
                outputStream.write(buf, 0, readNum);
            }
        } finally {
            fis.close();
        }
        byte[] bytes = outputStream.toByteArray();
        outputStream.flush();
        outputStream.close();
        return bytes;
    }

    /**
     * Save temp file
     *
     * @param originalFilename String
     * @param bytes byte[]
     * @param tempFolder String
     * @param date Date
     * @return String
     */
    public static String saveTempFile(String originalFilename, byte[] bytes, String tempFolder,
                                      Date date)
            throws IOException {
        if (date == null) {
            date = new Date();
        }
        originalFilename = originalFilename.replaceAll("[\\\\/:*?\"<>|%]", StringUtils.EMPTY);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        String fileName = calendar.get(Calendar.HOUR_OF_DAY) + StringUtils.EMPTY
                + calendar.get(Calendar.MINUTE) + StringUtils.EMPTY + calendar.get(Calendar.SECOND)
                + StringUtils.EMPTY + calendar.get(Calendar.MILLISECOND) + "_" + originalFilename;
        File file = new File(tempFolder + File.separator + createPathByDate(date));
        if (!file.exists()) {
            file.mkdirs();
        }
        File fileWrite = new File(file.getPath() + File.separator + fileName);
        try (FileOutputStream fos = new FileOutputStream(fileWrite)) {
            fos.write(bytes);
        }
        return file.getPath() + File.separator + fileName;
    }

    /**
     * Save file zip
     *
     * @param mapFile Map<String, byte[]>
     * @return byte[]
     */
    public static byte[] saveTempFileZip(Map<String, byte[]> mapFile) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(outputStream);
        for (Map.Entry<String, byte[]> mapEntry : mapFile.entrySet()) {
            ZipEntry zipEntry = new ZipEntry(mapEntry.getKey());
            zipEntry.setSize(mapEntry.getValue().length);
            zos.putNextEntry(zipEntry);
            zos.write(mapEntry.getValue());
            zos.closeEntry();
        }
        zos.close();
        byte[] bytes = outputStream.toByteArray();
        outputStream.flush();
        outputStream.close();
        return bytes;
    }

    /**
     * Create path by date
     *
     * @param date Date
     * @return String
     */
    public static String createPathByDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return year + "/"
                + (month < 10 ? "0" + month : month) + "/"
                + (day < 10 ? "0" + day : day);
    }

    /**
     * Create file name
     *
     * @param originalFilename String
     * @param date             Date
     * @return String
     */
    public static String createFileName(String originalFilename, Date date) {
        originalFilename = originalFilename.replaceAll("[\\\\/:*?\"<>|%]", StringUtils.EMPTY);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return originalFilename + "_" + calendar.get(Calendar.HOUR_OF_DAY) + StringUtils.EMPTY
                + calendar.get(Calendar.MINUTE) + StringUtils.EMPTY + calendar.get(Calendar.SECOND)
                + StringUtils.EMPTY + calendar.get(Calendar.MILLISECOND);
    }

    public static String getFilePath(String fullPath) {
        if (isStringNotBlank(fullPath)) {
            fullPath = fullPath
                    .replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));
            return FilenameUtils.getPath(fullPath);
        }
        return null;
    }

    public static String getFileName(String fullPath) {
        if (isStringNotBlank(fullPath)) {
            fullPath = fullPath
                    .replaceAll("[/\\\\]+", Matcher.quoteReplacement(System.getProperty("file.separator")));
            return FilenameUtils.getName(fullPath);
        }
        return null;
    }

    /**
     * Dinh dang tien te
     *
     * @param currency Double
     * @return String
     */
    public static String formatVNDCurrency(Double currency) {
        DecimalFormat formatter = new DecimalFormat("###,###,###.###");
        if (currency == null) {
            return "0";
        }
        return formatter.format(currency);
    }

    /**
     * Dinh dang tien te
     *
     * @param currency
     * @return
     */
    public static String formatVNDCurrency(Long currency) {
        DecimalFormat formatter = new DecimalFormat("###,###,###.###");
        if (currency == null) {
            return "0";
        }
        return formatter.format(currency);
    }

    public static String convertLowerParamContains(String value) {
        String result = value.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("%", "\\\\%")
                .replace("_", "\\\\_");
        return "%" + result + "%";
    }

    public static int getDiffYears(Date fromDate, Date toDate) {
        Calendar a = Calendar.getInstance();
        a.setTime(fromDate);
        Calendar b = Calendar.getInstance();
        b.setTime(toDate);
        int diff = b.get(Calendar.YEAR) - a.get(Calendar.YEAR);
        if (a.get(Calendar.MONTH) > b.get(Calendar.MONTH) ||
                (a.get(Calendar.MONTH) == b.get(Calendar.MONTH) && a.get(Calendar.DATE) > b.get(Calendar.DATE))) {
            diff--;
        }
        return diff;
    }

    public static String getCurrentTimeWithFullFormat(Date date) {
        return new SimpleDateFormat("HH:mm:ss").format(date);
    }

    public static String getCurrentTime(Date date) {
        return new SimpleDateFormat("HHmmss").format(date);
    }

    public static String getLastDayOfMonth(int year, int month) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(ConfigConstants.DATE_FORMAT);
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

            Date date = sdf2.parse(year + "-" + (month < 10 ? ("0" + month) : month) + "-01");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.add(Calendar.MONTH, 1);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.DATE, -1);

            Date lastDayOfMonth = calendar.getTime();

            return sdf.format(lastDayOfMonth);
        } catch (Exception e) {
            log.error("Loi! getLastDayOfMonth: " + e.getMessage(), e);
            return StringUtils.EMPTY;
        }
    }

    public static String getCurrentDateTime(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    public static Long nullToZero(Long value) {
        return value == null ? 0l : value;
    }

    public static Double nullToZero(Double value) {
        return value == null ? 0d : value;
    }

    public static String nullToEmpty(Object value) {
        return value == null ? StringUtils.EMPTY : String.valueOf(value);
    }

    public static String nullOrEmptyToValue(Object value, Object replace) {
        return (value == null || StringUtils.EMPTY.equals(String.valueOf(value).trim())) ? String.valueOf(replace) : String.valueOf(value);
    }

    public static boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$");
        Matcher matcher = pattern.matcher(email);

        return matcher.find();
    }

    public static boolean validatePhoneNumber(String number) {
        Pattern pattern = Pattern.compile("^(\\+84|0)(3|5|7|8|9)\\d{8}$");
        Matcher matcher = pattern.matcher(number);

        return matcher.find();
    }

    public static boolean validateUserName(String username) {
/*     Username consists of alphanumeric characters (a-zA-Z0-9), lowercase, or uppercase.
       Username allowed of the dot (.), underscore (_), and hyphen (-).
       The dot (.), underscore (_), or hyphen (-) must not be the first or last character.
       The dot (.), underscore (_), or hyphen (-) does not appear consecutively, e.g., java..regex
       The number of characters must be between 5 and 20.
       */
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$");
        Matcher matcher = pattern.matcher(username);
        return matcher.find();
    }

    public static String makeLikeParam(String s) {
        if (StringUtils.isEmpty(s)) return "%%";
        s = s.trim().toLowerCase()
                .replace("\\", "\\\\")
                .replace("\\t", "\\\\t")
                .replace("\\n", "\\\\n")
                .replace("\\r", "\\\\r")
                .replace("\\z", "\\\\z")
                .replace("\\b", "\\\\b")
                .replace("&", DEFAULT_ESCAPE_CHAR + "&")
                .replace("%", DEFAULT_ESCAPE_CHAR + "%")
                .replace("_", DEFAULT_ESCAPE_CHAR + "_");
        return "%" + s + "%";
    }

    public static String makeKeycloakPolicy(String roleCode) {
        return String.format("%s_POLICY", roleCode).toUpperCase();
    }

    public static String makeScopePermission(String roleCode, String resource) {
        return String.format("%s_%s_PERMISSION", roleCode, resource).toUpperCase();
    }

    //validate chuoi string chi chua ky tu thuong khong dau va dau cach
    public static boolean validateOnlyLowerCase(String str) {
        Pattern pattern = Pattern.compile("^[a-z0-9]+$");
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    public static void executeCommand(String command) throws IOException {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
        log.info("Command: {}", command);
        ProcessBuilder processBuilder = isWindows
                ? new ProcessBuilder("cmd.exe", "/c", command)
                : new ProcessBuilder("bash", "-c", command.replace("&", ";"));
        processBuilder.redirectErrorStream(true); // Redirect error stream to standard output

        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while (StringUtils.isNotBlank(line = reader.readLine())) {
            log.info("Command executed: {}", line);
        }
        log.info("Command executed, exit code: " + process);
//        process.destroy();
    }

    public static Pageable makePageable(BaseSearchDTO request) {
        int page = request.getPage() == null ? 0 : request.getPage();
        int pageSize = request.getPageSize() == null ? Integer.MAX_VALUE : request.getPageSize();
        if (CollectionUtils.isEmpty(request.getSorts())) {
            return PageRequest.of(page, pageSize);
        }
        String[] sorts = request.getSorts().get(0).split(":");
        Sort sort = Sort.by(Sort.Direction.valueOf(sorts[0].toUpperCase()), sorts[1]);
        return PageRequest.of(page, pageSize, sort);
    }
}
