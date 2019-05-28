package com.example.idamusic_mobile;

import java.util.List;

public class SpotifyDevices {
    public List<SpotifyDevice> devices;

    public SpotifyDevice getDeviceByName(String name) {
        for (SpotifyDevice dev : devices) {
            if (dev.name.equals(name)) return dev;
        }
        return null;
    }

    public SpotifyDevice getActiveDevice() {
        for (SpotifyDevice spot_device : devices) {
            if (spot_device.is_active == true) {
                return spot_device;
            }
        }
        return null;
    }

}