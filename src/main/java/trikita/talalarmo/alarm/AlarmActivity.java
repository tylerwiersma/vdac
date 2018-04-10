package trikita.talalarmo.alarm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import java.util.ArrayList;
import trikita.anvil.RenderableView;
import trikita.jedux.Action;
import trikita.talalarmo.Actions;
import trikita.talalarmo.App;
import trikita.talalarmo.ui.Theme;
import static trikita.anvil.DSL.FILL;
import static trikita.anvil.DSL.backgroundColor;
import static trikita.anvil.DSL.dip;
import static trikita.anvil.DSL.size;
import static trikita.anvil.DSL.text;
import static trikita.anvil.DSL.textColor;
import static trikita.anvil.DSL.textSize;


public class AlarmActivity extends Activity implements RecognitionListener {
    private PowerManager.WakeLock mWakeLock;


    /**
     * Variables needed through this class
     */
    private SpeechRecognizer speech = null;
    public Intent recognizerIntent;
    public String returnedText = "stop";
    public static final String[] VALUES = new String[] {"stop", "please no", "fuck off", "no", "can you don't", "help", "wake me up", "slurp"};


    /**
     * The onCreate runs as the alarm starts, so a few things need to be done here, such as making the alarm actually ring
     * @param b
     */
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "AlarmActivity");
        mWakeLock.acquire();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        Log.d("AlarmActivityListen", "OnCreate");

        // fill status bar with a theme dark color on post-Lollipop devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Theme.get(App.getState().settings().theme()).primaryDarkColor);
        }

        /**
         * Starts voice recording
         */
        startListening();

        /**
         * Sets the Theme, light or dark.
         */
        setContentView(new RenderableView(this) {
            public void view() {
                Theme.materialIcon(() -> {
                    size(FILL, FILL);
                    text("\ue857"); // "alarm off"
                    textColor(Theme.get(App.getState().settings().theme()).accentColor);
                    textSize(dip(128));
                    backgroundColor(Theme.get(App.getState().settings().theme()).backgroundColor);
                });
            }
        });
    }

    /**
     * When user leaves app, alarm stops
     */
    @Override
    protected void onUserLeaveHint() {
        stopAlarm();
        super.onUserLeaveHint();
    }

    /**
     * If the user presses the back button, it will stop
     */
    @Override
    public void onBackPressed() {
        stopAlarm();
        super.onBackPressed();
    }

    /**
     * More destruction of app handling
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWakeLock.release();
    }

    /**
     * Function to actually stop the alarm
     */
    private void stopAlarm() {
        App.dispatch(new Action<>(Actions.Alarm.DISMISS));
        finish();
    }

    /**
     * Handles resuming
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Handles pausing. Not sure how to utilize this though...
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.d("Log", "destroy");
        }

    }


    /**
     * Logs start of speech
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.d("Log", "onBeginningOfSpeech");
    }

    /**
     * Buffer handling. Not sure how this is utilized.
     * @param buffer
     */
    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.d("Log", "onBufferReceived: " + buffer);
    }

    /**
     * Handles the end of speech. Unused when apps merged.
     */
    @Override
    public void onEndOfSpeech() {
        Log.d("Log", "onEndOfSpeech");

    }

    /**
     * Handles the lovely errors one might receive
     * @param errorCode
     */
    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d("Log", "FAILED " + errorMessage);

    }

    /**
     * Called whenever an 'event' happens and logs it
     * @param arg0
     * @param arg1
     */
    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.d("Log", "onEvent");
    }

    /**
     * The meat and potatoes of how this app runs. Contains
     * @param arg0
     */
    @Override
    public void onPartialResults(Bundle arg0) {

        ArrayList<String> matches = arg0.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";

        for (String result : matches)
        {
            text += result + " ";
        }
        returnedText = text;



        for(int i =0; i < VALUES.length; i++)
        {
            if (returnedText.contains(VALUES[i])) {

                Log.d("text123456", "we in bois");
                stopAlarm();
            } else {
                startListening();
            }
        }


    }

    /**
     * Logs if listener is ready for speech
     * @param arg0
     */
    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.d("Log", "onReadyForSpeech");
    }

    /**
     * Logs when there are results
     * @param results
     */
    @Override
    public void onResults(Bundle results) {
        Log.d("Log", "onResults");

    }

    /**
     * Handles the start of the listening process
     */
    public void startListening() {
        Log.d("AlarmActivityListen", "Called startListening()");
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        /**
         * Minimum time to listen in milliseconds. Will listen for 10 seconds here.
         */
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 10000);
        recognizerIntent.putExtra("android.speech.extra.DICTATION_MODE", true);

        speech.startListening(recognizerIntent);

        speech.stopListening();




    }


    /**
     * On voice to text app it updated the progress bar
     * @param rmsdB
     */
    @Override
    public void onRmsChanged(float rmsdB) {
        Log.d("Log", "onRmsChanged: " + rmsdB);

    }

    /**
     * Retrieves error message
     * @param errorCode
     * @return
     */
    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}
