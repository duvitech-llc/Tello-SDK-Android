package com.duvitech.network.udp;

import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPVideoServer extends Thread {
    private static final String TAG = "UDPVideoServer";

    private DatagramSocket socket;
    private static boolean running;
    private byte[] buf = new byte[2048];

    private ByteArrayOutputStream encodedStream;

    private int Vide_W = 1920;
    private int Video_H = 1080;
    private int FrameRate = 25;
    private Boolean UseSPSandPPS = true;
    private byte[] header_sps = {0, 0, 0, 1, 103, 66, 0, 42, (byte) 149, (byte) 168, 30, 0, (byte) 137, (byte) 249, 102, (byte) 224, 32, 32, 32, 64};
    private byte[] header_pps = {0, 0, 0, 1, 104, (byte) 206, 60, (byte) 128, 0, 0, 0, 1, 6, (byte) 229, 1, (byte) 151, (byte) 128};


    public UDPVideoServer() throws SocketException {
        socket = new DatagramSocket(11111);
    }

    public void stopServer() {
        if (running) {
            Log.d(TAG, "Shutdown Requested");
            running = false;
            try {
                socket.close();
                this.join();
                Log.d(TAG, "Shutdown Completed");
            } catch (Exception e) {
                Log.e(TAG, "Error Shuting Down: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "Server Not Running or Stopping");
        }
    }

    @Override
    public void run() {
        Log.d(TAG, "Startup");
        int num = 0;
        running = true;
        encodedStream = new ByteArrayOutputStream();

        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
                int len = packet.getLength();
                encodedStream.write(packet.getData(),0, len);
                Log.d(TAG, "Data: " + len);
                if (len != 1460) {
                    /* decode frame */
                    // byte[] encodedFrame = encodedStream.toByteArray();
                    // Log.d(TAG, "Encoded Frame Length: " + encodedFrame.length);
                    // encodedStream.flush();
                    // encodedStream.reset();
                }
            } catch (IOException e) {
                if (running)
                    Log.e(TAG, "Error Receiving frame: " + e.getMessage());
            }
            num++;
        }
        socket.close();

        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"frame_" + num + ".264");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(encodedStream.toByteArray());
            encodedStream.flush();
            fos.close();
            encodedStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
