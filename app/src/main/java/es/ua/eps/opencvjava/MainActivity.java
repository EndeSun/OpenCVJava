package es.ua.eps.opencvjava;

import androidx.annotation.NonNull;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import android.Manifest;
import android.widget.TextView;
import com.google.android.material.slider.Slider;
import java.util.Collections;
import java.util.List;
import es.ua.eps.opencvjava.databinding.ActivityMainBinding;

public class MainActivity extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;
    Button startCamera;
    Mat mat;

    //SLIDERS
    Slider sliderBlur;
    Slider sliderEdge;
    Slider sliderAngle;
    //Sliders value
    TextView blurVal;
    TextView edgeVal;
    TextView angleVal;

    //ADJUST THREE PARAMETERS FOR THE FILTER
    private float blur;
    private float edge_gradient;
    private float angle;

    private int cameraCode = 101;

    private ActivityMainBinding binding;
    private boolean cannyEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {
            }

            //Procesamiento de los frames de los vídeos
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                return inputFrame.rgba();
            }
        });

        if(OpenCVLoader.initDebug()){
            cameraBridgeViewBase.enableView();
        } else{
            Log.d("LOADED", "err");
        }

        //BUTTON FOR APPLY OR DISABLE DE CANNY FILTER
        startCamera.setOnClickListener(v -> {
            cannyEnabled = !cannyEnabled;
            startCamera.setText(cannyEnabled ? "APAGAR FILTRO" : "FILTRO CANNY");
            if (cannyEnabled) {
                applyCannyFilter();
            } else {
                disableCannyFilter();
            }
        });


        sliderBlur.addOnChangeListener((slider, value, fromUser) -> {
            blurVal.setText(Float.toString(value));
            blur = value;
        });


        sliderEdge.addOnChangeListener((slider, value, fromUser) -> {
            edgeVal.setText(Float.toString(value));
            edge_gradient = value;
        });


        sliderAngle.addOnChangeListener((slider, value, fromUser) -> {
            angleVal.setText(Float.toString(value));
            angle = value;
        });



    }

    // APPLY CANNY FILTER
    private void applyCannyFilter() {
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }


            //MAIN PROCESS OF IMAGE PROCESSING
            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                Mat grayMat = new Mat();
                Mat blurredMat = new Mat();
                Mat edgesMat = new Mat();

                //blur (1,3,5)
                //edge_gradient (1-100)
                //angle (1,2,3,4,5)

                //rgb to gray
                Imgproc.cvtColor(inputFrame.rgba(), grayMat, Imgproc.COLOR_RGBA2GRAY);
                //Gaussian blur (blur)
                Imgproc.GaussianBlur(grayMat, blurredMat, new Size(blur,blur), 0);

                //threshold2 --> low_threshold(0, 100) (edge-gradient)
                //threshold1 --> ratio(1,2,3,4,5) (angle)
                Imgproc.Canny(blurredMat, edgesMat, edge_gradient, edge_gradient*angle);
                return edgesMat;
            }
        });
    }

    // DISABLE CANNY FILTER
    private void disableCannyFilter() {
        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            @Override
            public void onCameraViewStarted(int width, int height) {
            }

            @Override
            public void onCameraViewStopped() {
            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                // Solo devuelve el frame original sin aplicar ningún filtro
                return inputFrame.rgba();
            }
        });
    }


    void init(){
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        startCamera = binding.startButton;
        getPermission();
        cameraBridgeViewBase = binding.cameraView;
        sliderBlur = binding.sliderBlur;
        sliderEdge = binding.sliderEdge;
        sliderAngle = binding.sliderAngle;

        blurVal = binding.blurVal;
        edgeVal = binding.edgeVal;
        angleVal = binding.angleVal;

        blur = 3.0f;
        angle = 3.0f;
        edge_gradient = 60.0f;
    }






    //ACTIVITY LIFECICLE
    @Override
    protected void onResume() {
        super.onResume();
        cameraBridgeViewBase.enableView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();

    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

    //PERMISSIONS FOR THE CAMERA
    void getPermission(){
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, cameraCode);
        }
    }
    public void onRequestPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0]!=PackageManager.PERMISSION_GRANTED){
            getPermission();
        }
    }
}