package com.bot.writer;

import com.bot.domain.RowData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OutputReporter {

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
