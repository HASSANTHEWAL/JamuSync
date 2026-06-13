package com.quantalabs.jamusync.controller;

import com.quantalabs.jamusync.JamuSyncApp;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class CatalogController {

    @FXML
    public void initialize() {
        // Init catalog
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        JamuSyncApp.changeScene("/com/quantalabs/jamusync/fxml/Login.fxml", "JamuSync - Sign In");
    }
}
