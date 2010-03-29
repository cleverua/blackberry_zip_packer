package com.cleverua.bb.utils.zip;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import com.cleverua.bb.utils.IOUtils;

import net.rim.device.api.util.CRC32;

public class ZipArchiveEntry {

    private String fileName;
    private byte[] content;

    private static final int LOCAL_FILE_HEADER_SIGNATURE     = 0x04034B50;
    private static final int LOCAL_FILE_HEADER_CONSTANT_SIZE = 30;
    private static final int VERSION_FOR_EXTRACT             = 10;
    private static final int GENERAL_PURPOSE_BIT_FLAG        = 0;
    private static final int COMPRESSION_METHOD              = 0; // no compression
    private static final int EXTRA_FIELD_LENGTH              = 0;
    private static final int CENTRAL_FILE_HEADER_SIGNATURE   = 0x02014B50;
    private static final int VERSION_MADE_BY                 = 20;
    private static final int FILE_COMMENT_LENGTH             = 0;
    private static final int DISK_NUMBER_START               = 0;
    private static final int INTERNAL_FILE_ATTRS             = 0;
    private static final int EXT_FILE_ATTRS_FOR_FILE         = 32;
    private static final int EXT_FILE_ATTRS_FOR_DIR          = 16;

    private int localFileHeaderSignature;
    private int versionForExtract;
    private int generalPurposeBitFlag;
    private int compressionMethod;
    private int lastModified;
    private int crc32;
    private int compressedSize;
    private int uncompressedSize;
    private int fileNameLength;
    private int extraFieldLength;
    private int entrySize;
    private int centralFileHeaderSignature;
    private int versionMadeBy;
    private int fileCommentLength;
    private int diskNumberStart;
    private int internalFileAttrs;
    private int externalFileAttrs;

    ZipArchiveEntry(String fileName, String fullPath) throws IOException {
        this.fileName = fileName;
        this.lastModified = getCurrentDosTime();

        if (IOUtils.isDirectory(fullPath)) {
            externalFileAttrs = EXT_FILE_ATTRS_FOR_DIR;
            content = new byte[0];
            crc32 = 0;
        } else {
            externalFileAttrs = EXT_FILE_ATTRS_FOR_FILE;
            content = IOUtils.getFileData(fullPath);
            crc32 = CRC32.update(CRC32.INITIAL_VALUE, content) ^ 0xffffffff;
        }

        uncompressedSize = content.length;
        compressedSize = uncompressedSize;
        fileNameLength = this.fileName.length();

        entrySize = LOCAL_FILE_HEADER_CONSTANT_SIZE + fileNameLength + compressedSize;

        localFileHeaderSignature    = LOCAL_FILE_HEADER_SIGNATURE;
        versionForExtract           = VERSION_FOR_EXTRACT;
        generalPurposeBitFlag       = GENERAL_PURPOSE_BIT_FLAG;
        compressionMethod           = COMPRESSION_METHOD;
        extraFieldLength            = EXTRA_FIELD_LENGTH;
        centralFileHeaderSignature  = CENTRAL_FILE_HEADER_SIGNATURE;
        versionMadeBy               = VERSION_MADE_BY;
        fileCommentLength           = FILE_COMMENT_LENGTH;
        diskNumberStart             = DISK_NUMBER_START;
        internalFileAttrs           = INTERNAL_FILE_ATTRS;
    }

    void nullifyContent() {
        content = null;
    }

    int getLocalFileHeaderSignature() {
        return localFileHeaderSignature;
    }

    int getVersionForExtract() {
        return versionForExtract;
    }

    int getGeneralPurposeBitFlag() {
        return generalPurposeBitFlag;
    }

    int getCompressionMethod() {
        return compressionMethod;
    }

    int getLastModified() {
        return lastModified;
    }

    int getCRC32() {
        return crc32;
    }

    int getCompressedSize() {
        return compressedSize;
    }

    int getUncompressedSize() {
        return uncompressedSize;
    }

    int getFileNameLength() {
        return fileNameLength;
    }

    int getExtraFieldLength() {
        return extraFieldLength;
    }

    String getFileName() {
        return fileName;
    }

    byte[] getContent() {
        return content;
    }

    int getEntrySize() {
        return entrySize;
    }

    int getCentralFileHeaderSignature() {
        return centralFileHeaderSignature;
    }

    int getVersionMadeBy() {
        return versionMadeBy;
    }

    int getFileCommentLength() {
        return fileCommentLength;
    }

    int getDiskNumberStart() {
        return diskNumberStart;
    }

    int getInternalFileAttrs() {
        return internalFileAttrs;
    }

    int getExternalFileAttrs() {
        return externalFileAttrs;
    }

    private int getCurrentDosTime() {
        Calendar cal = Calendar.getInstance();
        synchronized (cal) {
            cal.setTime(new Date(System.currentTimeMillis()));
            int dostime = (cal.get(Calendar.YEAR) - 1980 & 0x7f) << 25
            | (cal.get(Calendar.MONTH) + 1) << 21
            | (cal.get(Calendar.DAY_OF_MONTH)) << 16
            | (cal.get(Calendar.HOUR_OF_DAY)) << 11
            | (cal.get(Calendar.MINUTE)) << 5
            | (cal.get(Calendar.SECOND)) >> 1;
            return dostime;
        }
    }
}