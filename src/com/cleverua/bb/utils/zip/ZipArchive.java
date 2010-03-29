package com.cleverua.bb.utils.zip;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import com.cleverua.bb.utils.IOUtils;

public class ZipArchive {

    private OutputStream out;
    private Vector entries;

    private static final int CENTRAL_DIR_END_SIGNATURE = 0x06054b50;
    private static final int CENTRAL_DIR_FILE_HEADER_CONSTANT_SIZE = 46;

    private int centralDirEndSignature;
    private int thisDiskNumber;
    private int thisDiskNumberWithStartOfCentralDir;
    private int centralDirEntriesNumberOnThisDisk;
    private int centralDirEntriesNumber;
    private int centralDirSize;
    private int centralDirOffsetRespectingStartDiskNumber;
    private int zipFileCommentLength;

    ZipArchive(OutputStream out) {
        this.out = out;
        entries  = new Vector();

        centralDirEndSignature                    = CENTRAL_DIR_END_SIGNATURE;
        thisDiskNumber                            = 0;
        thisDiskNumberWithStartOfCentralDir       = 0;
        centralDirEntriesNumberOnThisDisk         = 0;
        centralDirEntriesNumber                   = 0;
        centralDirSize                            = 0;
        centralDirOffsetRespectingStartDiskNumber = 0;
        zipFileCommentLength                      = 0;
    }

    void addEntry(ZipArchiveEntry entry) throws IOException {
        // write local file header for the entry
        writeLe4Bytes(entry.getLocalFileHeaderSignature());
        writeCommonHeadersData(entry);
        out.write(entry.getFileName().getBytes());
        out.write(entry.getContent());

        // let JVM know it can release the memory resource
        entry.nullifyContent();

        entries.addElement(entry);
    }

    void flush() throws IOException {

        final int entriesSize = entries.size();

        centralDirEntriesNumber = entriesSize;
        centralDirEntriesNumberOnThisDisk = entriesSize;

        // write central dir data
        for (int i = 0; i < entriesSize; i++) {
            ZipArchiveEntry entry = (ZipArchiveEntry) entries.elementAt(i);

            writeLe4Bytes(entry.getCentralFileHeaderSignature());
            writeLe2Bytes(entry.getVersionMadeBy());
            writeCommonHeadersData(entry);
            writeLe2Bytes(entry.getFileCommentLength());
            writeLe2Bytes(entry.getDiskNumberStart());
            writeLe2Bytes(entry.getInternalFileAttrs());
            writeLe4Bytes(entry.getExternalFileAttrs());
            writeLe4Bytes(centralDirOffsetRespectingStartDiskNumber); // localHeaderRelativeOffset for the entry
            out.write(entry.getFileName().getBytes());

            centralDirSize += (CENTRAL_DIR_FILE_HEADER_CONSTANT_SIZE + entry.getFileNameLength());
            centralDirOffsetRespectingStartDiskNumber += entry.getEntrySize();
        }

        writeLe4Bytes(centralDirEndSignature);
        writeLe2Bytes(thisDiskNumber);
        writeLe2Bytes(thisDiskNumberWithStartOfCentralDir);
        writeLe2Bytes(centralDirEntriesNumberOnThisDisk);
        writeLe2Bytes(centralDirEntriesNumber);
        writeLe4Bytes(centralDirSize);
        writeLe4Bytes(centralDirOffsetRespectingStartDiskNumber);
        writeLe2Bytes(zipFileCommentLength);

        out.flush();
    }

    void close() {
        IOUtils.safelyCloseStream(out);
    }

    private void writeLe2Bytes(int value) throws IOException {
        out.write(value & 0xff);
        out.write((value >> 8) & 0xff);
    }

    private void writeLe4Bytes(int value) throws IOException {
        writeLe2Bytes(value);
        writeLe2Bytes(value >> 16);
    }

    // this data is the same for the local and central file headers
    private void writeCommonHeadersData(ZipArchiveEntry entry) throws IOException {
        writeLe2Bytes(entry.getVersionForExtract());
        writeLe2Bytes(entry.getGeneralPurposeBitFlag());
        writeLe2Bytes(entry.getCompressionMethod());
        writeLe4Bytes(entry.getLastModified());
        writeLe4Bytes(entry.getCRC32());
        writeLe4Bytes(entry.getCompressedSize());
        writeLe4Bytes(entry.getUncompressedSize());
        writeLe2Bytes(entry.getFileNameLength());
        writeLe2Bytes(entry.getExtraFieldLength());
    }
}