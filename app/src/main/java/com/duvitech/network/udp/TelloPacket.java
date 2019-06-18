package com.duvitech.network.udp;

public class TelloPacket {

    static public final byte msgHdr = (byte)0xcc; // 204

    static public final byte ptExtended = 0;
    static public final byte ptGet = 1;
    static public final byte ptData1 = 2;
    static public final byte ptData2 = 4;
    static public final byte ptSet = 5;
    static public final byte ptFlip = 6;

    private class packet{
        public byte header;
        public short size13;
        public byte crc8;
        public boolean fromDrone; // the following 4 fields are encoded in a single byte in the raw packet
        public boolean toDrone;
        public byte packetType; // 3-bit
        public byte packetSubtype; // 3-bit
        public short messageID;
        public short sequence;
        public byte[] payload;
        public short crc16;
    };



}
