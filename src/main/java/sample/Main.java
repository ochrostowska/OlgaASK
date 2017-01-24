package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_objdetect;

import static org.bytedeco.javacpp.opencv_core.cvLoad;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

        Image image = new Image("lena.jpg");
        System.out.println(image.getHeight());

        String XML_FILE = "haarcascade_frontalface_default.xml";
        opencv_objdetect.CvHaarClassifierCascade cascade = new opencv_objdetect.CvHaarClassifierCascade(cvLoad(XML_FILE));
        System.out.println(cascade.isNull()); // bedzie true

        opencv_core.IplImage img = cvLoadImage("lena.jpg");
        System.out.println(img.isNull()); // bedzie true
        //System.out.println(img.height()); // wywali NullPointerException
    }


    public static void main(String[] args) {

        launch(args);

    }
}
