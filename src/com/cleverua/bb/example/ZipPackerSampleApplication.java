package com.cleverua.bb.example;

import net.rim.device.api.ui.UiApplication;

public class ZipPackerSampleApplication extends UiApplication {
    
    private static ZipPackerSampleApplication application;

    public static void main(String[] args) {
        application = new ZipPackerSampleApplication();
        application.pushScreen(new PlaceholderScreen());
        application.enterEventDispatcher();
    }
}
