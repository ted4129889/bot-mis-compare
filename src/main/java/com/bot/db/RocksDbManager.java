package com.bot.db;

import org.apache.commons.io.FileUtils;
import org.rocksdb.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class RocksDbManager implements AutoCloseable {

    private RocksDB db;
    private Options options;

    private  String dbPath;

    public RocksDbManager(String dbPath) throws RocksDBException {
        this.dbPath = dbPath;

        RocksDB.loadLibrary();

        File dir = new File(dbPath);
        validateManagedRocksDbPath(dir.toPath());

        //若資料夾存在，先刪除
        if (dir.exists()) {
            try {
                FileUtils.deleteDirectory(dir);
                System.out.println("RocksDB folder cleared: " + dbPath);
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete RocksDB directory: " + dbPath, e);
            }
        }

        // 建立資料夾
        if (!dir.mkdirs()) {
            throw new RuntimeException("Cannot create RocksDB data directory: " + dbPath);
        }

        this.options = new Options()
                .setCreateIfMissing(true)
                .setIncreaseParallelism(Runtime.getRuntime().availableProcessors())
                .setAllowMmapReads(true)
                .setAllowMmapWrites(true);
        this.db = RocksDB.open(options, dbPath);
    }

    private void validateManagedRocksDbPath(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        Path fileName = normalized.getFileName();
        Path parent = normalized.getParent();
        Path grandParent = parent == null ? null : parent.getParent();

        boolean isExpectedDbFolder = fileName != null
                && parent != null
                && grandParent != null
                && "A_DB".equals(fileName.toString())
                && "rocksdb".equals(parent.getFileName().toString())
                && normalized.toString().contains("ComparisonResult");

        if (!isExpectedDbFolder) {
            throw new IllegalArgumentException("Refuse to manage unexpected RocksDB path: " + normalized);
        }
    }

    public void put(String key, String value) throws RocksDBException {
        db.put(key.getBytes(), value.getBytes());
    }

    public String get(String key) throws RocksDBException {
        byte[] v = db.get(key.getBytes());
        return v == null ? null : new String(v);
    }

    public RocksIterator newIterator() {
        return db.newIterator();
    }

    @Override
    public void close() {
        db.close();
        options.close();
    }
}
