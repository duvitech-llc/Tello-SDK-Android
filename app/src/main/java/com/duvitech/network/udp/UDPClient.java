package com.duvitech.network.udp;

import android.util.Log;
import java.net.*;

public class UDPClient {
    private static final String TAG = "UDPClient";
    private DatagramSocket socket;
    private InetAddress address;

    public static final int PORT = 8889;
    public static final String ADDRESS = "192.168.10.1";


    public UDPClient() throws SocketException, UnknownHostException {
        connect();
    }

    public void sendCommand(String msg) {
        CommandThread commandThread = new CommandThread(address, socket, msg);
        commandThread.start();
    }


    public void sendBytes(byte[] bytes) {
        CommandThread commandThread = new CommandThread(address, socket, bytes);
        commandThread.start();
    }

    public void close() {
        if(!socket.isClosed()) {
            socket.close();
            Log.d(TAG, "Disconnecting");
        }
    }

    public void connect() throws SocketException, UnknownHostException {
        Log.d(TAG, "Connecting");
        socket = new DatagramSocket();
        address = InetAddress.getByName(ADDRESS);
    }
}