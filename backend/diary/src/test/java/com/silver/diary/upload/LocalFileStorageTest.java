package com.silver.diary.upload;

import static org.junit.jupiter.api.Assertions.*;

import com.silver.diary.support.TestData;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalFileStorageTest {
    @TempDir Path root;

    @Test
    void savePng() throws Exception {
        var storage = new LocalFileStorage(root.toString(), "/uploads//");
        String url = storage.save(TestData.png(), "covers");
        assertTrue(url.matches("/uploads/covers/[0-9a-f-]{36}\\.png"));
        assertTrue(Files.exists(root.resolve(url.substring("/uploads/".length()))));
        assertFalse(Files.exists(root.resolve("fake.html")));
    }

    @Test
    void badFolder() {
        var storage = new LocalFileStorage(root.toString(), "/uploads/");
        assertThrows(
                IllegalArgumentException.class, () -> storage.save(TestData.png(), "../outside"));
    }

    @Test
    void deleteOwn() throws Exception {
        var storage = new LocalFileStorage(root.toString(), "/uploads/");
        String url = storage.save(TestData.png(), "avatars");
        storage.deleteQuietly(url);
        assertFalse(Files.exists(root.resolve(url.substring(9))));
    }

    @Test
    void keepForeign() throws Exception {
        Path keep = root.resolve("keep.txt");
        Files.writeString(keep, "keep");
        var storage = new LocalFileStorage(root.toString(), "/uploads/");
        storage.deleteQuietly("/uploads/covers/../keep.txt");
        storage.deleteQuietly("https://other.invalid/keep.txt");
        assertEquals("keep", Files.readString(keep));
    }
}
