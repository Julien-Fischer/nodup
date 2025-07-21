package net.agiledeveloper.nodup.ui;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;

public class GUIDirectoryOpener implements DirectoryOpener {

    @Override
    public void open(Path directory) throws OpenException {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(directory.toFile());
            } catch (IOException exception) {
                throw OpenException.forDirectory(directory, exception);
            }
        } else {
            throw new OpenException("Desktop API is not supported on this platform.");
        }
    }

}
