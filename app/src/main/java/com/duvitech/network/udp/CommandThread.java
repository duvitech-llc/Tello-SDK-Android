package com.duvitech.network.udp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class CommandThread extends Thread {
    private static final String TAG = "CommandThread";
    private InetAddress address;
    private DatagramSocket socket;
    private byte[] msgBytes;

    public CommandThread(InetAddress address, DatagramSocket socket, String msg)  {
        this.address = address;
        this.msgBytes = msg.getBytes();
        this.socket = socket;
    }

    public CommandThread(InetAddress address, DatagramSocket socket, byte[] msg)  {
        this.address = address;
        this.msgBytes = msg;
        this.socket = socket;
    }

    @Override
    public void run() {
        DatagramPacket packet = new DatagramPacket(msgBytes, msgBytes.length, address, UDPClient.PORT);
        try {
            socket.send(packet);
            if(String.valueOf(msgBytes).compareTo("command")==0) {
                byte[] buf = new byte[500];
                packet = new DatagramPacket(buf, buf.length);
                socket.setSoTimeout(500);
                try {
                    socket.receive(packet);
                    String doneText = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                    Log.d(TAG, "Response: " + doneText);
                    if (doneText.compareTo("ok") == 0) {
                        Log.d(TAG, "Connected");
                    }
                }catch (SocketTimeoutException ste){
                    Log.e(TAG, ste.getMessage());
                }
            }
        } catch (SocketException se) {
            Log.e(TAG, se.getMessage());

        } catch (IOException e) {
            Log.e(TAG, "Command Error: " + e.getMessage());
        }
    }
}