package eventos;

import core.AppPaths;
import core.AppSettings;
import eventos.ui.console.ConsoleApp;

public class Main {

    public static void main(String[] args) {
        AppPaths.ensureDirs();
        if (!AppSettings.isLoaded())
            System.err.println("[Main] Warning: config.properties not loaded.");
        new ConsoleApp().run();
    }
}