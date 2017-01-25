package sample;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;


import javafx.stage.Modality;
import javafx.stage.Stage;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;
import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;

import static com.sun.tools.doclint.Entity.image;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_face.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;


public class WebCamPreviewController implements Initializable {


    @FXML
    private BorderPane bpWebCamPaneHolder;

    @FXML
    private FlowPane fpBottomPane;

    @FXML
    private ImageView imgWebCamCapturedImage;

    private static OpenCVFrameGrabber grabber = null;
    private boolean stopCamera = false;
    private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
    private OpenCVFrameConverter.ToIplImage converterToIPl = new OpenCVFrameConverter.ToIplImage();
    private BufferedImage buff = null;
    private IplImage iplImage = null;

    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
        fpBottomPane.setDisable(true);
        Platform.runLater(() -> setImageViewSize());
        initializeWebCam(0);
    }

    public IplImage detect(opencv_core.IplImage src, int option) throws IOException {

        //Define classifier
        opencv_objdetect.CvHaarClassifierCascade cascade = new opencv_objdetect.CvHaarClassifierCascade(cvLoad("src/main/resources/haarcascade_eye.xml"));
        opencv_core.CvMemStorage storage = opencv_core.CvMemStorage.create();

        //Detect objects
        opencv_core.CvSeq sign = cvHaarDetectObjects(
                src,
                cascade,
                storage,
                1.5,
                3,
                CV_HAAR_DO_CANNY_PRUNING);

        cvClearMemStorage(storage);

        ArrayList<MarkedRect> finalDetected = new ArrayList<>();
        int total_Faces = sign.total();
        for (int i = 0; i < total_Faces; i++) {
            opencv_core.CvRect r = new opencv_core.CvRect(cvGetSeqElem(sign, i));
            MarkedRect rect = new MarkedRect(r.x(), r.y(), r.width(), r.height());
            finalDetected.add(rect);
        }

        for (MarkedRect rect : finalDetected) {
            for (MarkedRect rectTocompare : finalDetected)
                if (rect != rectTocompare)
                    if (rect.getHeight() > rectTocompare.getHeight() && rect.getWidth() > rectTocompare.getWidth()
                            && rect.getxStart() < rectTocompare.getxStart() && rect.getxEnd() > rectTocompare.getxEnd()
                            && rect.getyStart() < rectTocompare.getyStart() && rect.getyEnd() > rectTocompare.getyEnd()) {
                        rect.setToRemove(true);
                    }
        }

        ArrayList<MarkedRect> finalllRects = new ArrayList<>();
        System.out.println(finalDetected.size());
        for (MarkedRect rect : finalDetected) {
            if (!rect.toRemove)
                finalllRects.add(rect);
        }

        BufferedImage myPhoto = IplImageToBufferedImage(src);
        BufferedImage eyeImage = null;
        BufferedImage mask = null;

        if (option == 0) {
            mask = generateMask(myPhoto, Color.PINK, 0.2f);
            eyeImage = ImageIO.read(getClass().getClassLoader().getResource("heart.png"));}
        if (option == 1) {
            mask = generateMask(myPhoto, Color.GREEN, 0.1f);
            eyeImage = ImageIO.read(getClass().getClassLoader().getResource("dd.png")); }
        if (option == 2) {
            mask = generateMask(myPhoto, Color.MAGENTA, 0.1f);
            eyeImage = ImageIO.read(getClass().getClassLoader().getResource("trump.png")); }
        if (option == 3) {
            mask = generateMask(myPhoto, Color.RED, 0.1f);
            eyeImage = ImageIO.read(getClass().getClassLoader().getResource("pizza.png")); }
        if (option == 4) {
            mask = generateMask(myPhoto, Color.BLUE, 0.1f);
            eyeImage = ImageIO.read(getClass().getClassLoader().getResource("puppy.png")); }


        BufferedImage tinted = tint(myPhoto, mask);
        Graphics g = tinted.getGraphics();
        for (MarkedRect r : finalllRects) {

            BufferedImage thumbnail = Scalr.resize(eyeImage, r.getWidth());
            g.drawImage(thumbnail, r.getxStart(), r.getyStart(), null);

//            cvRectangle (
//                    src,
//                    cvPoint(r.getxStart(), r.getyStart()),
//                    cvPoint(r.getWidth() + r.getxStart(), r.getHeight() + r.getyStart()),
//                    CvScalar.RED,
//                    2,
//                    CV_AA,
//                    0);

        }

        g.dispose();

        File outputfile = new File("imageRes.png");
        System.out.println(outputfile.getAbsolutePath());
        ImageIO.write(tinted, "png", outputfile);

        BufferedImage finalMyPhoto = tinted;
        Platform.runLater(() -> {
            final Image mainiamge = SwingFXUtils
                    .toFXImage(finalMyPhoto, null);
            imageProperty.set(mainiamge);
        });
        return src;

    }

    public static BufferedImage generateMask(BufferedImage imgSource, Color color, float alpha) {
        int imgWidth = imgSource.getWidth();
        int imgHeight = imgSource.getHeight();

        BufferedImage imgMask = createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
        Graphics2D g2 = imgMask.createGraphics();
        applyQualityRenderingHints(g2);

        g2.drawImage(imgSource, 0, 0, null);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, alpha));
        g2.setColor(color);

        g2.fillRect(0, 0, imgSource.getWidth(), imgSource.getHeight());
        g2.dispose();

        return imgMask;
    }
    public BufferedImage tint(BufferedImage master, BufferedImage tint) {
        int imgWidth = master.getWidth();
        int imgHeight = master.getHeight();

        BufferedImage tinted = createCompatibleImage(imgWidth, imgHeight, Transparency.TRANSLUCENT);
        Graphics2D g2 = tinted.createGraphics();
        applyQualityRenderingHints(g2);
        g2.drawImage(master, 0, 0, null);
        g2.drawImage(tint, 0, 0, null);
        g2.dispose();

        return tinted;
    }

    public static BufferedImage createCompatibleImage(int width, int height, int transparency) {
        BufferedImage image = new BufferedImage(width, height, transparency);
        image.coerceData(true);
        return image;
    }

    public static void applyQualityRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }


    public void showResult(BufferedImage photo) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resultView.fxml"));
        Parent root = loader.load();

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Your photo");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        Scene scene = new Scene(root);
        Controller controller = loader.getController();
        controller.loadPhoto(photo);
        dialogStage.setScene(scene);
        dialogStage.show();
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

    protected void initializeWebCam(int webCamIndex) {
        grabber = new OpenCVFrameGrabber(webCamIndex);

        Task<Void> webCamIntilizer = new Task<Void>() {

            @Override
            protected Void call() throws Exception {

                try {
                    grabber.setImageWidth((int) bpWebCamPaneHolder.getWidth());
                    grabber.setImageHeight((int) bpWebCamPaneHolder.getHeight());
                    grabber.start();
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        new Thread(webCamIntilizer).start();
        fpBottomPane.setDisable(false);
    }

    private void closeCamera() throws FrameGrabber.Exception {
        if (grabber.isTriggerMode()) grabber.stop();
    }

    @FXML
    public void onLove() throws FrameGrabber.Exception {
        stopCamera = true;
        closeCamera();
        IplImage finalIplImage = iplImage;
        Platform.runLater(() -> {
            try {
                detect(finalIplImage, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onTrump() throws FrameGrabber.Exception {
        stopCamera = true;
        closeCamera();
        IplImage finalIplImage = iplImage;
        Platform.runLater(() -> {
            try {
                detect(finalIplImage, 2);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onPizza() throws FrameGrabber.Exception {
        stopCamera = true;
        closeCamera();
        IplImage finalIplImage = iplImage;
        Platform.runLater(() -> {
            try {
                detect(finalIplImage, 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onPuppy() throws FrameGrabber.Exception {
        stopCamera = true;
        closeCamera();
        IplImage finalIplImage = iplImage;
        Platform.runLater(() -> {
            try {
                detect(finalIplImage, 4);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void onMoney() throws FrameGrabber.Exception {
        stopCamera = true;
        closeCamera();
        IplImage finalIplImage = iplImage;
        Platform.runLater(() -> {
            try {
                detect(finalIplImage, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    @FXML
    public void startCamera() throws FrameGrabber.Exception {
        stopCamera = false;
        startCamerka();
    }


    protected void startCamerka() throws FrameGrabber.Exception {

        Task<Void> task = new Task<Void>() {
            Frame capturedFrame = null;

            @Override
            protected Void call() throws Exception {

                Java2DFrameConverter paintConverter = new Java2DFrameConverter();
                while (!stopCamera) {
                    try {
                        if ((capturedFrame = grabber.grab()) != null) {
                            iplImage = converterToIPl.convert(capturedFrame);
                            buff = paintConverter.getBufferedImage(capturedFrame, 1);
                            Platform.runLater(() -> {
                                final Image mainiamge = SwingFXUtils
                                        .toFXImage(buff, null);
                                imageProperty.set(mainiamge);

                            });
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
        imgWebCamCapturedImage.imageProperty().bind(imageProperty);
    }

    public static BufferedImage IplImageToBufferedImage(IplImage src) {
        OpenCVFrameConverter.ToIplImage grabberConverter = new OpenCVFrameConverter.ToIplImage();
        Java2DFrameConverter paintConverter = new Java2DFrameConverter();
        Frame frame = grabberConverter.convert(src);
        return paintConverter.getBufferedImage(frame, 1);
    }
}





