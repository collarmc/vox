package team.catgirl.vox.audio.devices;

import javax.sound.sampled.Mixer;

public abstract class Device<T> {
    private T line;
    private Mixer.Info info;

    public Device(T line, Mixer.Info info) {
        this.line = line;
        this.info = info;
    }

    public String getDescription() {
        return info.getDescription();
    }

    public String getIdentifer() {
        return info.getName();
    }

    public Mixer.Info getInfo() {
        return info;
    }

    public T getLine() {
        return line;
    }

    public String getName() {
        return info.getName() != null ? info.getName() : "Microphone";
    }

    public String getVendor() {
        return info.getVendor();
    }

    public String getVersion() {
        return info.getVersion();
    }
}
