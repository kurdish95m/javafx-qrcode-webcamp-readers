/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package QRcodeReader;

import com.github.sarxos.webcam.Webcam;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/**
 *
 * @author roots
 */
public class FXMLDocumentController implements Initializable {

    @FXML
    private TextField textfield;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<>();
    private QRDecoder qrDecoder = new QRDecoder();

    private Webcam webcam;
    @FXML
    private ImageView ivCameraOutput;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
        startCameraInput();
    }

    private void startCameraInput() {
        Task<Void> webCamTask = new Task<Void>() {
            @Override
            protected Void call() {
                webcam = Webcam.getDefault();
                webcam.open();
                startWebCamStream();
                return null;
            }
        };

        Thread webCamThread = new Thread(webCamTask);
        webCamThread.setDaemon(true);
        webCamThread.start();
    }

    private void startWebCamStream() {
        boolean stopCamera = false;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                final AtomicReference<WritableImage> ref = new AtomicReference<>();
                BufferedImage img;
                //noinspection ConstantConditions,LoopConditionNotUpdatedInsideLoop
                int i=0;
                while (!stopCamera) {
                    try {
                        if ((img = webcam.getImage()) != null) {
                            ref.set(SwingFXUtils.toFXImage(img, ref.get()));
                            img.flush();
                            Platform.runLater(() -> imageProperty.set(ref.get()));

                            String scanResult = qrDecoder.decodeQRCode(img);
                            if (scanResult != null) {
                                System.out.println(i + " " +scanResult);
                                textfield.setText(scanResult);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                return null;
            }
        };

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
        ivCameraOutput.imageProperty().bind(imageProperty);
    }
}
