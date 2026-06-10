package eventos;

import core.AppPaths;
import core.AppSettings;
import eventos.ui.gui.MainWindow;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        AppPaths.ensureDirs();
        if (!AppSettings.isLoaded())
            System.err.println("[Main] Warning: config.properties not loaded.");

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignore) {}

        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}
