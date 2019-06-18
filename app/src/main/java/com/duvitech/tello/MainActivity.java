package com.duvitech.tello;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.duvitech.network.udp.UDPClient;
import com.duvitech.network.udp.UDPVideoServer;

import org.freedesktop.gstreamer.GStreamer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity implements SurfaceHolder.Callback{

    private static final String TAG = "TelloDemo";

    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING
    private Button btnVideo;

    private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    UDPVideoServer vid;
    UDPClient client;

    // Get paired devices.
    public void getPairedBluetoothDevices() {
        Set<BluetoothDevice> pairedDevices = bAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("BT DEVICES", "Device Name: " + deviceName + " Address: " + deviceHardwareAddress);
            }
        }
    }

    public ArrayList<Integer> getGameControllerIds() {
        ArrayList<Integer> gameControllerDeviceIds = new ArrayList<Integer>();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                    Log.d(TAG,"Found GAME CONTROLLER: " + deviceId);
                }
            }
        }
        return gameControllerDeviceIds;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        btnVideo = findViewById(R.id.btnVideo);
        btnVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(vid == null) {
                    try {
                        is_playing_desired = true;
                        nativePlay();
                        byte[] cmd = new String("streamon".getBytes(), StandardCharsets.UTF_8).getBytes();
                        client.sendBytes(cmd);
                        btnVideo.setText(getString(R.string.stop_video));
                    } catch (Exception ex) {
                        Log.e(TAG, "Vid Server Error: " + ex.getMessage());
                    }
                }else{
                    try {
                        byte[] cmd = new String("streamoff".getBytes(), StandardCharsets.UTF_8).getBytes();
                        client.sendBytes(cmd);
                        is_playing_desired = false;
                        nativePause();
                        btnVideo.setText(getString(R.string.start_video));
                    } catch (Exception ex) {
                        Log.e(TAG, "Vid Server Error: " + ex.getMessage());
                    }

                }

            }
        });

        SurfaceView sv = (SurfaceView) this.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        if (savedInstanceState != null) {
            is_playing_desired = savedInstanceState.getBoolean("playing");
            Log.i ("GStreamer", "Activity created. Saved state is playing:" + is_playing_desired);
        } else {
            is_playing_desired = false;
            Log.i ("GStreamer", "Activity created. There is no saved state, playing: false");
        }


        try {
            client = new UDPClient();
            byte[] cmd = new String("command".getBytes(), StandardCharsets.UTF_8).getBytes();
            client.sendBytes(cmd);

        }catch(Exception ex){
            Log.e(TAG, "Comm Error: " +ex.getMessage());
        }

        getPairedBluetoothDevices();
        getGameControllerIds();
        nativeInit();
    }

    protected void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d ("GStreamer", "Saving state, playing:" + is_playing_desired);
        outState.putBoolean("playing", is_playing_desired);
    }


    protected void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }


    private static float getCenteredAxis(MotionEvent event,
                                         InputDevice device, int axis, int historyPos) {
        final InputDevice.MotionRange range =
                device.getMotionRange(axis, event.getSource());

        // A joystick at rest does not always report an absolute position of
        // (0,0). Use the getFlat() method to determine the range of values
        // bounding the joystick axis center.
        if (range != null) {
            final float flat = range.getFlat();
            final float value =
                    historyPos < 0 ? event.getAxisValue(axis):
                            event.getHistoricalAxisValue(axis, historyPos);

            // Ignore axis values that are within the 'flat' region of the
            // joystick axis center.
            if (Math.abs(value) > flat) {
                return value;
            }
        }
        return 0;
    }

    private void processJoystickInput(MotionEvent event,
                                      int historyPos) {

        InputDevice inputDevice = event.getDevice();

        // Calculate the horizontal distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat axis, or the right control stick.
        float lx = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_X, historyPos);

        float hx = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_HAT_X, historyPos);

        float rx = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Z, historyPos);


        // Calculate the vertical distance to move by
        // using the input value from one of these physical controls:
        // the left control stick, hat switch, or the right control stick.
        float ly = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_Y, historyPos);

        float hy = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_HAT_Y, historyPos);

        float ry = getCenteredAxis(event, inputDevice,
                MotionEvent.AXIS_RZ, historyPos);

        // Update the ship object based on the new x and y values
        Log.d("GC", "LS-X: " + lx + " LS-Y: " + ly);
        Log.d("GC", "HT-X: " + hx + " HT-Y: " + hy);
        Log.d("GC", "RS-X: " + rx + " RS-Y: " + ry);
        if (client != null) {
            int d = (int)(lx * 100.0);
            int c = (int)(ly * -100.0);
            int a = (int)(rx * 100.0);
            int b = (int)(ry * -100.0);

            if(hx != 0 || hy != 0){
                if(hx > 0){
                    client.sendCommand("flip r");
                }else if(hx < 0){
                    client.sendCommand("flip l");
                }

                if(hy < 0){
                    client.sendCommand("flip f");
                }else if(hy > 0){
                    client.sendCommand("flip b");
                }

            }else {
                client.sendCommand("rc " + a + " " + b + " " + c + " " + d);
            }

        }else{
            Log.e(TAG,"Client is NULL");
        }

    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {

        // Check that the event came from a game controller
        if ((event.getSource() & InputDevice.SOURCE_JOYSTICK) ==
                InputDevice.SOURCE_JOYSTICK &&
                event.getAction() == MotionEvent.ACTION_MOVE) {

            // Process all historical movement samples in the batch
            final int historySize = event.getHistorySize();

            // Process the movements starting from the
            // earliest historical position in the batch
            for (int i = 0; i < historySize; i++) {
                // Process the event at historical position i
                processJoystickInput(event, i);
            }

            // Process the current movement sample in the batch (position -1)
            processJoystickInput(event, -1);
            return true;
        }
        return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {
            if (event.getRepeatCount() == 0) {
                Log.d("GC", KeyEvent.keyCodeToString(keyCode));
                switch (keyCode) {
                    // Handle gamepad and D-pad button presses to
                    // navigate the ship
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        handled = true;

                        break;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        handled = true;

                        break;
                    case KeyEvent.KEYCODE_BUTTON_X:
                        handled = true;

                        break;
                    case KeyEvent.KEYCODE_BUTTON_L1:
                        handled = true;
                        if(client != null)
                            client.sendCommand("takeoff");
                        break;
                    case KeyEvent.KEYCODE_BUTTON_R1:
                        handled = true;
                        if(client != null)
                            client.sendCommand("land");

                        break;
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        handled = true;

                        break;
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        handled = true;

                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        handled = true;

                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        handled = true;

                        break;
                    default:
                        if (isFireKey(keyCode)) {
                            // Update the ship object to fire lasers
                            Log.d("GAME CONTROLLER", "Fired");
                            handled = true;
                        }
                        break;
                }
            }
            if (handled) {
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private static boolean isFireKey(int keyCode) {
        // Here we treat Button_A and DPAD_CENTER as the primary action
        // keys for the game.
        return keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_BUTTON_A;
    }


    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "Gst initialized");
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }

        // Re-enable buttons, now that GStreamer is initialized
        final Activity activity = this;
        runOnUiThread(new Runnable() {
            public void run() {
                Button btnTemp = activity.findViewById(R.id.btnVideo);
                if(is_playing_desired){
                    btnTemp.setText("Stop");
                }else{
                    btnTemp.setText("Start");
                }
            }
        });
    }
    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        Log.i ("GStreamer", "Set UI Message");
        final TextView tv = (TextView) this.findViewById(R.id.textview_message);
        runOnUiThread (new Runnable() {
            public void run() {
                tv.setText(message);
            }
        });

    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("gstTelloVideo");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyed");
        nativeSurfaceFinalize ();
    }
}
