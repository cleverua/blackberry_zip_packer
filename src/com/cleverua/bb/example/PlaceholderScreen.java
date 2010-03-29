package com.cleverua.bb.example;

import java.io.IOException;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

import com.cleverua.bb.utils.IOUtils;
import com.cleverua.bb.utils.zip.ZipPacker;

public class PlaceholderScreen extends MainScreen {

    private static final String SCREEN_TITLE = "Zip Packer Demo";
    private static final String BUTTON_LABEL = "Pack";

    private static final String DIR_TO_PACK = "file:///SDCard/dir_to_pack/";
    private static final String OUTPUT_FILE = "file:///SDCard/packed.zip";

    private ButtonField testLoggerButton;

    public PlaceholderScreen() {
        super();
        initUI();
    }

    private void initUI() {
        setTitle(SCREEN_TITLE);

        getMainManager().setPadding(10, 0, 0, 0);

        testLoggerButton = new ButtonField(BUTTON_LABEL, FIELD_HCENTER);

        testLoggerButton.setChangeListener(new FieldChangeListener() {
            public void fieldChanged(Field f, int c) {

                if (!IOUtils.isSDCardPresent()) {
                    Dialog.alert("Please, insert SDCard to run this application.");
                    return;
                }

                try {
                    // create a file structure to pack 
                    IOUtils.createDirIncludingAncestors(DIR_TO_PACK + "dir_2/dir_3/");
                    IOUtils.saveDataToFile(
                            DIR_TO_PACK + "file_1.txt", 
                            new byte[] { 'c', 'o', 'n', 't', 'e', 'n', 't', ' ', '1' }
                    );
                    IOUtils.saveDataToFile(
                            DIR_TO_PACK + "dir_2/file_2.txt", 
                            new byte[] { 'c', 'o', 'n', 't', 'e', 'n', 't', ' ', '2' }
                    );
                    IOUtils.saveDataToFile(
                            DIR_TO_PACK + "dir_2/dir_3/file_3.txt", 
                            new byte[] { 'c', 'o', 'n', 't', 'e', 'n', 't', ' ', '3' }
                    );

                } catch (IOException e) {
                    Dialog.alert("Failed to create demo file structure to be packed: " + e);
                    return;
                }

                try {
                    ZipPacker.pack(DIR_TO_PACK, OUTPUT_FILE);
                    Dialog.inform('\'' + OUTPUT_FILE + "' has been successfully created.");
                } catch (Exception e) {
                    Dialog.alert("Failed to pack the '" + DIR_TO_PACK + "' directory: " + e);
                }
            }
        });

        add(testLoggerButton);
    }

    protected boolean onSavePrompt() {
        return true;
    }
}