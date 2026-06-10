package eventos;

import core.AppPaths;
import core.AppSettings;
import eventos.ui.gui.MainWindow;
import eventos.ui.gui.Theme;

import javax.swing.*;

/**
 * Main.java — actualizado para aplicar el tema Office dark antes de abrir la ventana.
 * Reemplaza el bloque de UIManager del original.
 */
public class Main {

    public static void main(String[] args) {
        AppPaths.ensureDirs();
        if (!AppSettings.isLoaded())
            System.err.println("[Main] Warning: config.properties not loaded.");

        // Aplica el tema antes de crear cualquier componente Swing
        Theme.apply();

        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
