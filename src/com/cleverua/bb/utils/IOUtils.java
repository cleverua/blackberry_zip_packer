package com.cleverua.bb.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.file.FileIOException;
import net.rim.device.api.system.Characters;

/**
 * A bunch of convenient methods for file IO manipulations.
 * @author Vit Khudenko, vit@cleverua.com
 */
public class IOUtils {

    public static final String CARD_ROOT = "file:///SDCard/";

    /**
     * Any file gets this ".rem" extension if SDCard Encryption is ON.
     * A media file gets the ".rem" extension if MediaFile Encryption is ON.
     */
    private static final String ENCR_FILE_EXTENSION = ".rem";

    private static final String TMP_EXT = ".tmp";
    private static final String URL_ROOT_SEPARATOR = ":///";

    /**
     * Safely closes {@link InputStream} stream.
     * 
     * <p>
     * If stream is not null, the method calls <code>close()</code> for the stream.<br />
     * If any {@link Exception} occurs during <code>close()</code> call, that exception is silently caught.
     * </p>
     * 
     * @param stream {@link InputStream} instance to be closed.
     */
    public static void safelyCloseStream(InputStream stream) {
        if (stream != null) {
            try { 
                stream.close();
                stream = null;
            } catch (Exception e) { /* that's ok */ }
        }
    }

    /**
     * Safely closes {@link OutputStream} stream.
     * 
     * <p>
     * If stream is not null, the method calls <code>close()</code> for the stream.<br />
     * If any {@link Exception} occurs during <code>close()</code> call, 
     * that exception is caught and the only action being made is putting 
     * a log message about the got exception.
     * </p>
     * 
     * <p>
     * <b>IMPORTANT:</b><br />
     * Despite <code>close()</code> call also invokes <code>flush()</code> first,
     * clients should not relay on this behavior and must call <code>flush()</code>
     * explicitly before calling <code>safelyCloseStream(OutputStream stream)</code> instead.
     * Otherwise any useful exception <code>flush()</code> may throw will not be passed to the client
     * and the stream will remain open.
     * </p>
     * 
     * @param stream {@link OutputStream} instance to be closed.
     */
    public static void safelyCloseStream(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
                stream = null;
            } catch (Exception e) {
                //Logger.debug(IOUtils.class, "got error in safelyCloseStream for OutputStream: " + e);
            }
        }
    }

    /**
     * Safely closes {@link FileConnection} stream.
     * 
     * <p>
     * If stream is not null, the method calls <code>close()</code> for the stream.<br />
     * If any {@link Exception} occurs during <code>close()</code> call, that exception is silently caught.
     * </p>
     * 
     * @param stream {@link FileConnection} instance to be closed.
     */
    public static void safelyCloseStream(FileConnection stream) {
        if (stream != null) {
            try {
                stream.close();
                stream = null;
            } catch (Exception e) { /* that's ok */ }
        }
    }

    /**
     * Saves byte array data to file with a given url.
     * If the destination file has been already present, then it is overwritten.
     * 
     * @param data - Array of bytes to save.
     * @param url - url of the destination file.
     * @throws IOException
     * <ul>
     * <li>if the <code>url</code> is invalid.</li>
     * <li>if the target file system is not accessible or the data array size is greater 
     * than free memory that is available on the file system the file resides on.</li>
     * <li>if an I/O error occurs.</li>
     * <li>if url has a trailing "/" to denote a directory, or an unspecified error occurs preventing creation of the file.</li>
     * </ul>
     */
    public static void saveDataToFile(String url, byte[] data) throws IOException {
        FileConnection fc  = null;
        FileConnection tmp = null;
        OutputStream out   = null;

        try {
            fc = (FileConnection) Connector.open(url);

            // check for available space
            if (fc.availableSize() < data.length) {
                throw new FileIOException(FileIOException.FILESYSTEM_FULL);
            }

            if (fc.exists()) {

                tmp = (FileConnection) Connector.open(url + TMP_EXT);

                if (tmp.exists()) {
                    tmp.delete(); /* just in case */
                }
                tmp.create();

                try {
                    out = tmp.openOutputStream();
                    out.write(data);
                    out.flush();
                } catch (IOException e) {
                    safelyCloseStream(out);
                    try {
                        tmp.delete();
                    } catch (IOException e1) { 
                        /* do nothing here */
                    }
                    throw e;
                }

                String originalFileName = fc.getName();
                fc.delete();
                tmp.rename(originalFileName);

            } else {
                fc.create();
                out = fc.openOutputStream();
                out.write(data);
                out.flush();
            }

        } finally {
            safelyCloseStream(out);
            safelyCloseStream(fc);
            safelyCloseStream(tmp);
        }
    }

    /**
     * Reads file data and returns it as a byte array. 
     * File should be present, otherwise IOException is thrown. 
     * 
     * @param url - url of the source file.
     * @return Array of bytes.
     * @throws IOException
     * <ul>
     * <li>if the <code>url</code> is invalid.</li>
     * <li>if an I/O error occurs, if the method is invoked on a directory, 
     * the file does not yet exist, or the connection's target is not accessible.</li>
     * </ul>
     */
    public static byte[] getFileData(String url) throws IOException {
        FileConnection fc = null;
        InputStream in = null;

        try {
            fc = (FileConnection) Connector.open(url);
            in = fc.openInputStream();
            byte[] data = new byte[(int) fc.fileSize()];
            in.read(data);
            return data;
        } finally {
            safelyCloseStream(in);
            safelyCloseStream(fc);
        }
    }

    /**
     * Creates a directory corresponding to passed <code>url</code> parameter. 
     * Directories in the specified <code>url</code> are not recursively created and 
     * must be explicitly created before subdirectories can be created. If the directory
     * is already exists, then the method does nothing. 
     * 
     * @param url - path to dir to create, e.g. <code>"file:///SDCard/my_new_dir/"</code>.
     * @throws IOException
     * <ul> 
     * <li>if the <code>url</code> is invalid.</li>
     * <li>if invoked on a non-existent file, the target file system is not accessible, 
     * or an unspecified error occurs preventing creation of the directory.</li>
     * </ul>
     */
    public static void createDir(String url) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(url);
            if (!fc.exists()) {
                fc.mkdir();
            }
        } finally {
            safelyCloseStream(fc);
        }
    }

    /**
     * Creates a directory corresponding to passed <code>url</code> parameter. 
     * Unlike {@link #createDir createDir(String url)} directories in the 
     * specified <code>url</code> are recursively created if needed. If the directory
     * is already exists, then the method does nothing. 
     * 
     * @param url - path to dir to create, e.g. <code>"file:///SDCard/my_new_dir/"</code>.
     * @throws IllegalArgumentException if <code>url</code> does not 
     * include <code>":///"</code> substring.
     * @throws IOException
     * <ul> 
     * <li>if the <code>url</code> is invalid.</li>
     * <li>if invoked on a non-existent file, the target file system is not accessible, 
     * or an unspecified error occurs preventing creation of the directory.</li>
     * </ul>
     */
    public static void createDirIncludingAncestors(String url) throws IOException {
        final int index = url.indexOf(URL_ROOT_SEPARATOR);
        if (index == -1) {
            throw new IllegalArgumentException("Invalid url");
        }
        String rootOfPath = url.substring(0, index + 4); // e.g. "file:///"
        String restOfPath = url.substring(rootOfPath.length());
        int solidusIndex = -1;
        while (true) {
            solidusIndex = restOfPath.indexOf(Characters.SOLIDUS, solidusIndex + 1);
            if (solidusIndex < 0) {
                break;
            }
            createDir(rootOfPath + restOfPath.substring(0, solidusIndex + 1));
        }
    }

    /**
     * @param url - target path to check, e.g. <code>"file:///SDCard/my_new_dir/"</code>.
     * @return True if the target exists, is accessible, and is a directory, otherwise false.
     * @throws IOException if the <code>url</code> is invalid.
     */
    public static boolean isDirectory(String url) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(url, Connector.READ);
            return fc.isDirectory();
        } finally {
            IOUtils.safelyCloseStream(fc);
        }
    }

    /**
     * If user enables SDCard Encryption, then files may get the ".rem" extension.
     * To get "clean" file url we may use this method. We need a clean url to pass
     * it to some other IO method.
     * 
     * @param url to clean from ".rem" extension.
     * @return url with ".rem" extension removed if found. Null remains null.
     */
    public static String removeEncExtension(String url) {
        if (url != null && url.endsWith(ENCR_FILE_EXTENSION)) {
            return url.substring(0, (url.length() - ENCR_FILE_EXTENSION.length()));
        }
        return url;
    }

    /**
     * Checks if the file or directory specified in the <code>url</code> 
     * passed to the method exists.
     * 
     * @param url - URL to a file or directory to be processed.
     * @return true if the target exists and is accessible, otherwise false.
     * @throws IOException - if the <code>url</code> is invalid.
     * @throws SecurityException - if the security of the application 
     * does not have read access for the <code>url</code>.
     */
    public static boolean isPresent(String url) throws IOException {
        FileConnection fc = null;
        try {
            fc = (FileConnection) Connector.open(url, Connector.READ);
            return fc.exists();
        } finally {
            IOUtils.safelyCloseStream(fc);
        }
    }

    /**
     * @return true if SDCard is present or false otherwise.
     */
    public static boolean isSDCardPresent() {
        try {
            return isPresent(CARD_ROOT);
        } catch (IOException e) {
            return false;
        }
    }
}
