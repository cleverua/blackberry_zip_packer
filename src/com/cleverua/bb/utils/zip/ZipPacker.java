package com.cleverua.bb.utils.zip;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.cleverua.bb.utils.IOUtils;

/**
 * ZipPacker is able to pack a bunch of files into a one ZIP archive format file.
 * Note, ZipPacker uses zero compression only.
 * 
 * @author Vit Khudenko, vit@cleverua.com
 */
public class ZipPacker {

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private String dirToPack;
    private String outFilename;
    private String relativeItemPathStart;

    /**
     * Packs directories/files found at <code>dirToZip</code> into a one ZIP archive format file.
     * 
     * <p>If target already exists - it will be overwritten.</p>
     * 
     * @param dirToPack - url of a directory to pack.
     * @param outputFileFullPath - url for the resulting ZIP file.
     * @throws IOException
     */
    public static void pack(String dirToPack, String outputFileFullPath) throws IOException {
        ZipPacker z = new ZipPacker(dirToPack, outputFileFullPath);
        z.pack();
    }

    private ZipPacker(String dirToPack, String outputFileFullPath) {
        this.dirToPack = dirToPack;
        this.outFilename = outputFileFullPath;
        this.relativeItemPathStart = getRelativeItemPathStart(); 
    }

    private void pack() throws IOException {

        // These are the files to include in the ZIP file
        Vector filePaths = new Vector();

        collectFilePaths(dirToPack, filePaths);

        FileConnection zipFC = null;
        ZipArchive archive = null;

        try {
            zipFC = (FileConnection) Connector.open(outFilename);

            if (zipFC.exists()) {
                zipFC.delete();
            }
            zipFC.close();

            zipFC = (FileConnection) Connector.open(outFilename);
            zipFC.create();

            archive = new ZipArchive(zipFC.openOutputStream());

            // Pack the files
            final int size = filePaths.size();
            for (int i = 0; i < size; i++) {
                String entryFullPath = (String) filePaths.elementAt(i);
                archive.addEntry(new ZipArchiveEntry(getRelativeItemPath(entryFullPath), entryFullPath));
            }

            archive.flush();

        } finally {
            if (archive != null) {
                archive.close();
                archive = null;
            }
            IOUtils.safelyCloseStream(zipFC);
        }
    }

    private void collectFilePaths(String path, Vector paths) throws IOException {
        FileConnection fc = null;

        try {
            fc = (FileConnection) Connector.open(path, Connector.READ);

            if (fc.isDirectory()) {
                Enumeration e = fc.list();
                IOUtils.safelyCloseStream(fc);
                while (e.hasMoreElements()) {
                    collectFilePaths(IOUtils.removeEncExtension(path + ((String) e.nextElement())), paths);
                }
            }

            if (!path.equals(dirToPack)) {
                paths.addElement(IOUtils.removeEncExtension(path));
            }

        } finally {
            IOUtils.safelyCloseStream(fc);
        }
    }

    private String getRelativeItemPathStart() {
        String trimmed = dirToPack.substring(0, dirToPack.length() - 2);
        return dirToPack.substring(trimmed.indexOf(FILE_SEPARATOR));
    }

    private String getRelativeItemPath(String fullItemPath) {
        return fullItemPath.substring(fullItemPath.indexOf(relativeItemPathStart) +
                relativeItemPathStart.length());
    }
}
