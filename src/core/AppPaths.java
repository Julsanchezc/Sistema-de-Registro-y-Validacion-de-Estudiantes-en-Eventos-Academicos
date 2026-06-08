package core;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class AppPaths {

    public static final Path BASE = Paths.get(System.getProperty("user.dir"));

    public static final class Dirs {
        public static final Path DATA    = BASE.resolve("data");
        public static final Path LOGS    = BASE.resolve("logs");
        public static final Path RESULTS = BASE.resolve("results");

        private Dirs() {}
    }

    public static final class Files {
        public static final Path EVENTS_JSON = Dirs.DATA.resolve("events.json");
        public static final Path APP_LOG     = Dirs.LOGS.resolve("app.log");

        private Files() {}
    }

    private AppPaths() {}

    public static void ensureDirs() {
        try {
            java.nio.file.Files.createDirectories(Dirs.DATA);
            java.nio.file.Files.createDirectories(Dirs.LOGS);
            java.nio.file.Files.createDirectories(Dirs.RESULTS);
        } catch (IOException e) {
            System.err.println("[AppPaths] Failed to create directories: " + e.getMessage());
        }
    }
}
