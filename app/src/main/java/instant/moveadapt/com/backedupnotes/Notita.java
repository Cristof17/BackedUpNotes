package instant.moveadapt.com.backedupnotes;

import java.util.UUID;

/**
 * Created by cristof on 16.08.2017.
 */

public class Notita {

    private int id;
    private long createTimestamp;
    private long modifiedTimestamp;
    private boolean modified;
    private String note;
    private UUID uuid;

    public Notita(int id, long createTimestamp, long modifiedTimestamp, boolean modified, String uuid, String note) {
        this.id = id;
        this.createTimestamp = createTimestamp;
        this.modifiedTimestamp = modifiedTimestamp;
        this.modified = modified;
        this.note = note;
        this.uuid = UUID.fromString(uuid);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public void setModifiedTimestamp(long modifiedTimestamp) {
        this.modifiedTimestamp = modifiedTimestamp;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public int getId() {
        return id;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public long getModifiedTimestamp() {
        return modifiedTimestamp;
    }

    public boolean getModified() {
        return modified;
    }

    public String getNote() {
        return note;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
