package com.codex.pvphud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/** Replace config files only after a complete, valid JSON document is ready. */
public final class JsonStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private JsonStorage() {}
    public static synchronized boolean write(Path path, Object value) {
        Path temporary = null;
        try {
            String json = GSON.toJson(value);
            Files.createDirectories(path.toAbsolutePath().getParent());
            temporary = Files.createTempFile(path.toAbsolutePath().getParent(), path.getFileName().toString(), ".tmp");
            Files.writeString(temporary, json);
            try { Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (AtomicMoveNotSupportedException unsupported) {
                Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException | RuntimeException failure) {
            return false;
        } finally {
            if (temporary != null) try { Files.deleteIfExists(temporary); } catch (IOException ignored) {}
        }
    }
    public static void backup(Path path) {
        try {
            if (Files.exists(path)) Files.copy(path, path.resolveSibling(path.getFileName() + ".broken-" + System.nanoTime()));
        } catch (IOException ignored) {}
    }
}
