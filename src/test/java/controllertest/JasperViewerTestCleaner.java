package controllertest;

import java.awt.EventQueue;
import java.awt.Window;

public final class JasperViewerTestCleaner {

    private JasperViewerTestCleaner() {
    }

    public static void disposeOpenWindows() {
        EventQueue.invokeLater(JasperViewerTestCleaner::disposeSwingWindows);
    }

    private static void disposeSwingWindows() {
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
    }
}