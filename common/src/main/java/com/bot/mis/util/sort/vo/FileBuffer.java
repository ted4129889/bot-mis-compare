/* (C) 2024 */
package com.bot.mis.util.sort.vo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;

public class FileBuffer implements Comparable<FileBuffer> {
    private BufferedReader reader;
    private String currentLine;
    private SubstringComparator comparator;

    public FileBuffer(File file, SubstringComparator comparator, Charset charset)
            throws IOException {
        this.reader = new BufferedReader(new FileReader(file, charset));
        this.currentLine = reader.readLine();
        this.comparator = comparator;
    }

    public boolean isEmpty() {
        return currentLine == null;
    }

    public String readLine() throws IOException {
        String result = currentLine;
        currentLine = reader.readLine();
        return result;
    }

    public void close() throws IOException {
        reader.close();
    }

    @Override
    public int compareTo(FileBuffer other) {
        return comparator.compare(this.currentLine, other.currentLine);
    }
}
