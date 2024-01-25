package com.luminaryn.common

import java.io.*
import java.util.*

object Files {
    @JvmStatic
    fun getExtension(filename: String): String {
        return File(filename).extension;
    }

    @JvmStatic
    fun isImage(filename: String): Boolean {
        val ext = getExtension(filename).uppercase(Locale.getDefault())
        return when (ext) {
            "JPG", "PNG", "JPEG", "WEBP", "HEIC", "GIF" -> true
            else -> false
        }
    }

    @JvmStatic
    fun isVideo(filename: String): Boolean {
        val ext = getExtension(filename).uppercase(Locale.getDefault())
        return when (ext) {
            "MP4", "MKV", "AVI", "WEBM", "M4V", "MPG", "MPEG", "MPEG4", "MOV" -> true
            else -> false
        }
    }

    /**
     * Split a file into chunks.
     *
     * @param srcFile The file we want to split into chunks. Must exist and not be a directory.
     * @param outDir The directory we want to save the chunks into. Must exist and be a directory.
     * @param chunkSize The size of the chunks, in bytes.
     * @param startAt1 Start the chunk files at 001 instead of 000.
     *
     * @return Number of chunks the file was split into, or an error code.
     */
    @JvmOverloads
    fun split(srcFile: File, outDir: File, chunkSize: Long, startAt1: Boolean = false): Int {

        if (!srcFile.exists()) return ERR_SPLIT_NO_SRC
        if (srcFile.isDirectory) return ERR_SPLIT_SRC_DIR
        if (!outDir.exists()) return ERR_SPLIT_NO_OUT
        if (!outDir.isDirectory) return ERR_SPLIT_OUT_DIR

        val maxChunks = if (startAt1) MAX_FILE_CHUNKS-1 else MAX_FILE_CHUNKS

        val srcStream = BufferedInputStream(FileInputStream(srcFile))
        val srcSize = srcFile.length()
        val srcName = srcFile.name
        val chunks = srcSize / chunkSize

        if (chunks > maxChunks) {
            return ERR_SPLIT_MAX_CHUNKS
        }

        val getOutputFile = fun(cnt: Int): BufferedOutputStream {
            val num = if (startAt1) cnt+1 else cnt
            val ext = "%03d".format(num)
            return BufferedOutputStream(FileOutputStream("$outDir/$srcName.$ext"))
        }

        var f = 0
        while (f < chunks) {
            val out = getOutputFile(f)
            for (currentByte in 0 until chunkSize) {
                out.write(srcStream.read())
            }
            out.close()
            f++
        }

        if (srcSize != chunkSize * (f - 1)) {
            val out = getOutputFile(f)
            var b: Int
            while (srcStream.read().also { b = it } != -1) out.write(b)
            out.close()
        }

        srcStream.close()

        return f
    }

    /**
     * Split a file into chunks.
     *
     * @param srcFile The file we want to split into chunks.
     * @param outPath The path to the directory we want to save the chunks into.
     * @param chunkSize The size of the chunks, in bytes.
     * @param startAt1 Start the chunk files at 001 instead of 000.
     *
     * @return Number of chunks the file was split into, or an error code.
     */
    @JvmOverloads
    fun split(srcFile: File, outPath: String, chunkSize: Long, startAt1: Boolean = false): Int {
        return split(srcFile, File(outPath), chunkSize, startAt1)
    }

    /**
     * Split a file into chunks.
     *
     * This version saves the chunks into the same directory as the original file.
     *
     * @param srcFile The file we want to split into chunks.
     * @param chunkSize The size of the chunks, in bytes.
     * @param startAt1 Start the chunk files at 001 instead of 000.
     */
    @JvmOverloads
    fun split(srcFile: File, chunkSize: Long, startAt1: Boolean = false): Int {
        val outPath = srcFile.parent ?: "/"
        return split(srcFile, File(outPath), chunkSize, startAt1)
    }

    /**
     * Get a segment of a file without generating an intermediate file.
     */
    fun getSegment(srcFile: File, chunkSize: Long, wantPart: Int, startAt1: Boolean = false): ByteArrayOutputStream?
    {
        if (!srcFile.exists()) return null
        if (srcFile.isDirectory) return null

        val srcStream = BufferedInputStream(FileInputStream(srcFile))
        val chunks = getChunks(srcFile, chunkSize)

        val partIndex = if (startAt1 && wantPart > 0) wantPart-1 else wantPart

        if (partIndex >= chunks) return null

        val baStream = ByteArrayOutputStream()
        val boStream = BufferedOutputStream(baStream)

        srcStream.skip(partIndex*chunkSize)

        for (currentByte in 0 until chunkSize) {
            val byte = srcStream.read()
            if (byte == -1) break
            boStream.write(byte)
        }

        boStream.close()
        return baStream
    }

    @JvmOverloads
    fun getChunks(totalSize: Long, chunkSize: Long, addPartial: Boolean = true): Long {
        if (chunkSize == 0L || totalSize == 0L) // No dividing by zero thanks.
            return if (addPartial) 1 else 0     // 1 part min if including partial chunks.
        var chunks = totalSize / chunkSize
        if (totalSize != chunkSize * chunks)
            chunks++
        return chunks
    }

    fun getChunks(file: File, chunkSize: Long, addPartial: Boolean = true): Long {
        if (file.isDirectory || !file.exists()) return -1 // Not valid, cannot continue.
        return getChunks(file.length(), chunkSize, addPartial)
    }

    fun getChunks(path: String, chunkSize: Long, addPartial: Boolean = true): Long {
        return getChunks(File(path), chunkSize, addPartial)
    }

    /**
     * Convert KiB to bytes.
     */
    fun KB (kb: Long): Long {
        return kb * 1024
    }

    /**
     * Convert MiB to bytes.
     */
    fun MB (mb: Long): Long {
        return mb * 1024 * 1024
    }

    /**
     * Convert GiB to bytes.
     */
    fun GB (gb: Long): Long {
        return gb * 1024 * 1024 * 1024
    }

    /**
     * The maximum number of chunks for split() function.
     *
     * We're hard-coding this to 999 here as it's a sane maximum, and anything over this
     * breaks the standard file extension for chunks.
     *
     * If you set the startAt1 option to true, then the maximum number of chunks will be reduced
     * by 1 since we are eliminating the 000 file.
     */
    const val MAX_FILE_CHUNKS = 999;

    /**
     * The srcFile passed to split() does not exist.
     */
    const val ERR_SPLIT_NO_SRC = -1

    /**
     * The srcFile passed to split() was a directory.
     */
    const val ERR_SPLIT_SRC_DIR = -2

    /**
     * The outDir passed to split() does not exist.
     */
    const val ERR_SPLIT_NO_OUT = -3

    /**
     * The outDir pathed to split() was not a directory.
     */
    const val ERR_SPLIT_OUT_DIR = -4

    /**
     * The number of chunks generated by split() would exceed the maximum.
     */
    const val ERR_SPLIT_MAX_CHUNKS = -5

}
