package sample;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    BorderPane bpWebCamPaneHolder;

    @FXML
    ImageView imgWebCamCapturedImage;

    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void loadPhoto(BufferedImage photo) {
        Platform.runLater(() -> {
            final Image mainiamge = SwingFXUtils
                    .toFXImage(photo, null);
            imageProperty.set(mainiamge);
            imgWebCamCapturedImage.imageProperty().bind(imageProperty);
            setImageViewSize();
        });

    }

    protected void setImageViewSize() {

        double height = bpWebCamPaneHolder.getHeight();
        double width = bpWebCamPaneHolder.getWidth();
        imgWebCamCapturedImage.setFitHeight(height);
        imgWebCamCapturedImage.setFitWidth(width);
        imgWebCamCapturedImage.prefHeight(height);
        imgWebCamCapturedImage.prefWidth(width);
        imgWebCamCapturedImage.setPreserveRatio(true);

    }
}
