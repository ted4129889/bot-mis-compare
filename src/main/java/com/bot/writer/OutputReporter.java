package com.bot.writer;

import com.bot.domain.RowData;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class OutputReporter implements AutoCloseable {

    private final Map<Path, BufferedWriter> writers = new HashMap<>();

    public void writeExtra(RowData raw, Path extraPath) throws IOException {
        writeLine(extraPath, raw.toString());
    }

    public void writeFieldDiff(String result, Path diffPath) throws IOException {
        writeLine(diffPath, result);
    }

    public void writeMissing(String jsonA, Path missPath) throws IOException {
        writeLine(missPath, "MISSING(B撠?: " + jsonA);
    }

    public void writeFileA(RowData rawA, Path aPath) throws IOException {
        writeLine(aPath, rawA.toString());
    }

    public void writeFileB(RowData rawB, Path bPath) throws IOException {
        writeLine(bPath, rawB.toString());
    }

    private void writeLine(Path path, String text) throws IOException {
        BufferedWriter writer = writerFor(path);
        writer.write(text);
        writer.newLine();
    }

    private BufferedWriter writerFor(Path path) throws IOException {
        Path normalizedPath = path.toAbsolutePath().normalize();
        BufferedWriter existing = writers.get(normalizedPath);
        if (existing != null) {
            return existing;
        }

        Path parent = normalizedPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        BufferedWriter created = Files.newBufferedWriter(
                normalizedPath,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
        writers.put(normalizedPath, created);
        return created;
    }

    @Override
    public void close() throws IOException {
        IOException closeException = null;

        for (BufferedWriter writer : writers.values()) {
            try {
                writer.close();
            } catch (IOException e) {
                if (closeException == null) {
                    closeException = e;
                } else {
                    closeException.addSuppressed(e);
                }
            }
        }
        writers.clear();

        if (closeException != null) {
            throw closeException;
        }
    }

    public static synchronized void reportExtra(RowData raw, Path extraPath) throws IOException {

        Files.writeString(extraPath, raw.toString() + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static synchronized void reportFieldDiff(String result, Path diffPath)
            throws IOException {

        Files.writeString(diffPath,
                result + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static synchronized void reportMissing(String jsonA, Path missPath) throws IOException {
        Files.writeString(missPath, "MISSING(B少): " + jsonA + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }


    public static synchronized void reportFileA(RowData rawA, Path aPath) throws IOException {

        Files.writeString(aPath, rawA.toString() + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static synchronized void reportFileB(RowData rawB, Path aPath) throws IOException {
        Files.writeString(aPath, rawB.toString() + "\n",
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }


}
