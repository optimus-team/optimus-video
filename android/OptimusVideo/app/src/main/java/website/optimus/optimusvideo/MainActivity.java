package website.optimus.optimusvideo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.majorkernelpanic.streaming.Session;
import net.majorkernelpanic.streaming.SessionBuilder;
import net.majorkernelpanic.streaming.audio.AudioQuality;
import net.majorkernelpanic.streaming.gl.SurfaceView;
import net.majorkernelpanic.streaming.video.VideoQuality;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements Session.Callback, SurfaceHolder.Callback {

    private final static String TAG = "MainActivity";

    private TextView mText;
    private ImageButton mStartButton;
    private Button mSwitchCamera;
    private SurfaceView mSurfaceView;
    private EditText mEditText;
    private Session mSession;
    private ProgressBar mProgress;
    private TextView mStatus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mStatus = (TextView) findViewById(R.id.status);
        mStartButton = (ImageButton) findViewById(R.id.start_button);
        mSwitchCamera = (Button) findViewById(R.id.switch_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        mEditText = (EditText) findViewById(R.id.editText1);
        mText = (TextView) findViewById(R.id.text);
        mProgress = (ProgressBar) findViewById(R.id.progress);

        mSession = SessionBuilder.getInstance()
                .setCallback(this)
//                .setPreviewOrientation(180)
                .setSurfaceView(mSurfaceView)
                .setContext(getApplicationContext())
                .setAudioEncoder(SessionBuilder.AUDIO_AAC)
                .setAudioQuality(new AudioQuality(16000, 16000))
                .setVideoEncoder(SessionBuilder.VIDEO_H263)
                .setVideoQuality(new VideoQuality(640, 480, 15, 60000))
                .build();

        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onStartButtonClicked();
                } catch (IOException e) {
                    onSessionError(0, 0, e);
                }
            }
        });
        mSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSwitchCameraButtonClicked();
            }
        });

        mSurfaceView.getHolder().addCallback(this);

    }


    static final String SERVER_URL = "http://optimus.website:80";

    int[] getPorts() throws IOException {
        URL url = new URL(SERVER_URL + "/port");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        Scanner in = new Scanner(connection.getInputStream());
        return new int[]{in.nextInt(), in.nextInt()};
    }

    public void onStartButtonClicked() throws IOException {
        // Starts/stops streaming
        mSession.setDestination(mEditText.getText().toString());
        if (!mSession.isStreaming()) {
            postData();
        } else {
            mSession.stop();
//            mSession.stopPreview();
            mStartButton.setImageResource(R.drawable.ic_cam_inactive);
            setStatus("Not recording ^^");
        }
        mStartButton.setEnabled(false);
    }

    public void onSwitchCameraButtonClicked() {
        // Switch between the two cameras
        mSession.switchCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSession.isStreaming()) {
            mStartButton.setImageResource(R.drawable.ic_cam_active);
        } else {
            mStartButton.setImageResource(R.drawable.ic_cam_inactive);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSession.release();
    }

    @Override
    public void onBitrateUpdate(long bitrate) {
        Log.d(TAG, "Bitrate: " + bitrate);
    }

    @Override
    public void onSessionError(int message, int streamType, Exception e) {
        mStartButton.setEnabled(true);
        if (e != null) {
            logError(e.getMessage());
        }
    }

    @Override

    public void onPreviewStarted() {
        Log.d(TAG, "Preview started.");
    }

    @Override
    public void onSessionConfigured() {
        Log.d(TAG, "Preview configured.");
        // Once the stream is configured, you can get a SDP formated session description
        // that you can send to the receiver of the stream.
        // For example, to receive the stream in VLC, store the session description in a .sdp file
        // and open it with VLC while streming.
        mText.setText(mSession.getSessionDescription());
        Log.e(TAG, mSession.getSessionDescription());
        mSession.start();
    }

    @Override
    public void onSessionStarted() {
        Log.d(TAG, "Session started.");
        mStartButton.setEnabled(true);
        mStartButton.setImageResource(R.drawable.ic_cam_active);
    }

    @Override
    public void onSessionStopped() {
        Log.d(TAG, "Session stopped.");
        mStartButton.setEnabled(true);
        mStartButton.setImageResource(R.drawable.ic_cam_inactive);
    }

    /**
     * Displays a popup to report the eror to the user
     */
    private void logError(final String msg) {
        final String error = (msg == null) ? "Error unknown" : msg;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(error).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSession.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSession.stop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    public void postData() {
        new AsyncTask<Void, String, Exception>() {

            @Override
            protected void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                mStartButton.setVisibility(View.GONE);
            }

            @Override
            protected Exception doInBackground(Void... voids) {
                try {
                    publishProgress("Getting ports...");
                    int[] ports = getPorts();
                    mSession.setVideoPort(ports[0]);
                    mSession.setAudioPort(ports[1]);
                    publishProgress("PORTS " + ports[0] + " " + ports[1] + ". Configuring session");
                    mSession.configure();
                    publishProgress("Sleep for a while...");
                    Thread.sleep(250);
                    String data = "sdp=" +
                            URLEncoder.encode(mSession.getSessionDescription(), "UTF-8");

                    URL url = new URL(SERVER_URL + "/video");
                    publishProgress("Making connection to " + url);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");

                    connection.setFixedLengthStreamingMode(data.getBytes().length);
                    connection.setRequestProperty("Content-Type",
                            "application/x-www-form-urlencoded");
                    PrintWriter out = new PrintWriter(connection.getOutputStream());
                    out.print(data);
                    out.close();

                    publishProgress("Wrote POST");

                    connection.getResponseMessage();

                    publishProgress("Got response");

                    connection.disconnect();

                } catch (Exception e) {
                    e.printStackTrace();
                    return e;
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(String... values) {
                setStatus(values[0]);
            }


            @Override
            protected void onPostExecute(Exception e) {
                if (e == null) {
                    setStatus("No errors. R E C O R D I N G");
//                    onSessionError(0, 0, new Exception("No errors ^^"));
                    mStartButton.setImageResource(R.drawable.ic_cam_active);
                } else {
                    setStatus("N O T recording: " + e.getMessage());
                    mSession.stop();
                    mStartButton.setImageResource(R.drawable.ic_cam_inactive);
                    onSessionError(0, 0, e);
                    e.printStackTrace();
                }
                mProgress.setVisibility(View.GONE);
                mStartButton.setVisibility(View.VISIBLE);
            }
        }.execute();
    }

    void setStatus(String s) {
        mStatus.setText(s);
    }
}