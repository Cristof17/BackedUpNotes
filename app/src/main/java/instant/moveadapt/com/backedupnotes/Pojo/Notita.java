package instant.moveadapt.com.backedupnotes.Pojo;

import java.util.UUID;

/**
 * Created by cristof on 16.08.2017.
 */

public class Notita {

    public UUID id;
    public String text;
    public long timestamp;

    public Notita(UUID id, String text, long timestamp) {
        this.id = id;
        this.text = text;
        this.timestamp = timestamp;
    }
}
