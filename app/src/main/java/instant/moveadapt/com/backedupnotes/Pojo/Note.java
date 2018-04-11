package instant.moveadapt.com.backedupnotes.Pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.UUID;

/**
 * Created by cristof on 16.08.2017.
 */

public class Note implements Parcelable, Comparable<Note>{

    public String id;
    public String text;
    public Long timestamp;

    public Note(){
        this.id = null;
        this.text = null;
        this.timestamp = null;
    }

    public Note(String id, String text, long timestamp) {
        this.id = id;
        this.text = text;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        /*
         * Follow this order when deparcelling
         */
        dest.writeString(id.toString());
        dest.writeString(text);
        dest.writeLong(timestamp);
    }

    public static final Parcelable.Creator<Note> CREATOR = new
            Parcelable.Creator<Note>(){

                /*
                 * Follow the order when parcelling
                 */
                @Override
                public Note createFromParcel(Parcel source) {

                    String parcelId = source.readString();
                    String parcelText = new String(source.readString());
                    long parcelTimestamp = source.readLong();

                    return new Note(parcelId, parcelText, parcelTimestamp);
                }

                @Override
                public Note[] newArray(int size) {
                    return new Note[size];
                }
            };

    @Override
    public int compareTo(@NonNull Note o) {
        return id.toString().compareTo(o.id.toString());
    }

    @Override
    public boolean equals(Object note) {
        boolean contains = this.id.toString().equals( ((Note) note).id.toString());
        return contains;
    }

    /**
     * Hash code must be overriden because if we leave it the same
     * we don't know if the default implementation creates hashes ONLY based on  the
     * fields in this class, So two Notes might return different hashes for the same
     * field values for fields (id, text, timestamp) because it may add other hashes from
     * object class which may not be the same
     *
     * @return
     */
    @Override
    public int hashCode() {
        return 37 * id.hashCode() + text.hashCode() + timestamp.hashCode();
    }
}
