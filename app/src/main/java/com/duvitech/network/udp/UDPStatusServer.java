package com.duvitech.network.udp;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPStatusServer extends Thread {
    private static final String TAG = "UDPStatusServer";

    private DatagramSocket socket;
    private boolean running;
    private byte[] buf = new byte[256];

    public UDPStatusServer() throws SocketException {
        socket = new DatagramSocket(8890);
    }

    @Override
    public void run() {
        running = true;

        while (running) {
            DatagramPacket packet
                    = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            packet = new DatagramPacket(buf, buf.length, address, port);
            String received
                    = new String(packet.getData(), 0, packet.getLength());

            if(received != null)
                Log.d(TAG, received.trim());

            if (received.equals("end")) {
                running = false;
                continue;
            }
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socket.close();
    }
}