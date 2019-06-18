package com.duvitech.network.udp;

import java.util.HashMap;
import java.util.Map;

public enum TelloMessageID {
    Connect             ((byte) 0x0001), // 1
    Connected           ((byte) 0x0002), // 2
    QuerySSID           ((byte) 0x0011), // 17
    SetSSID             ((byte) 0x0012), // 18
    QuerySSIDPass       ((byte) 0x0013), // 19
    SetSSIDPass         ((byte) 0x0014), // 20
    QueryWifiRegion     ((byte) 0x0015), // 21
    SetWifiRegion       ((byte) 0x0016), // 22
    WifiStrength        ((byte) 0x001a), // 26
    SetVideoBitrate     ((byte) 0x0020), // 32
    SetDynAdjRate       ((byte) 0x0021), // 33
    EisSetting          ((byte) 0x0024), // 36
    QueryVideoSPSPPS    ((byte) 0x0025), // 37
    QueryVideoBitrate   ((byte) 0x0028), // 40
    DoTakePic           ((byte) 0x0030), // 48
    SwitchPicVideo      ((byte) 0x0031), // 49
    DoStartRec          ((byte) 0x0032), // 50
    ExposureVals        ((byte) 0x0034), // 52 (Get or set?)
    LightStrength       ((byte) 0x0035), // 53
    QueryJPEGQuality    ((byte) 0x0037), // 55
    Error1              ((byte) 0x0043), // 67
    Error2              ((byte) 0x0044), // 68
    QueryVersion        ((byte) 0x0045), // 69
    SetDateTime         ((byte) 0x0046), // 70
    QueryActivationTime ((byte) 0x0047), // 71
    QueryLoaderVersion  ((byte) 0x0049), // 73
    SetStick            ((byte) 0x0050), // 80
    DoTakeoff           ((byte) 0x0054), // 84
    DoLand              ((byte) 0x0055), // 85
    FlightStatus        ((byte) 0x0056), // 86
    SetHeightLimit      ((byte) 0x0058), // 88
    DoFlip              ((byte) 0x005c), // 92
    DoThrowTakeoff      ((byte) 0x005d), // 93
    DoPalmLand          ((byte) 0x005e), // 94
    FileSize            ((byte) 0x0062), // 98
    FileData            ((byte) 0x0063), // 99
    FileDone            ((byte) 0x0064), // 100
    DoSmartVideo        ((byte) 0x0080), // 128
    SmartVideoStatus    ((byte) 0x0081), // 129
    LogHeader           ((byte) 0x1050), // 4176
    LogData             ((byte) 0x1051), // 4177
    LogConfig           ((byte) 0x1052), // 4178
    DoBounce            ((byte) 0x1053), // 4179
    DoCalibration       ((byte) 0x1054), // 4180
    SetLowBattThresh    ((byte) 0x1055), // 4181
    QueryHeightLimit    ((byte) 0x1056), // 4182
    QueryLowBattThresh  ((byte) 0x1057), // 4183
    SetAttitude         ((byte) 0x1058), // 4184
    QueryAttitude       ((byte) 0x1059); // 4185)

    private static final Map<Byte, TelloMessageID> typesByValue = new HashMap<Byte, TelloMessageID>();

    static {
        for (TelloMessageID type : TelloMessageID.values()) {
            typesByValue.put(type.value, type);
        }
    }

    private final byte value;
    TelloMessageID(byte val) {
        this.value = val;
    }

    byte value() { return value; }
    public static TelloMessageID forValue(byte value) {
        return TelloMessageID.typesByValue.get(value);
    }

}
