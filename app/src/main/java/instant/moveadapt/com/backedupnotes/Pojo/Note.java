package instant.moveadapt.com.backedupnotes.Pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.UUID;

/**
 * Created by cristof on 16.08.2017.
 */

public class Note implements Parcelable, Comparable<Note>{

    public UUID id;
    public String text;
    public long timestamp;

    public Note(UUID id, String text, long timestamp) {
        this.id = id;
        this.text = text;
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

                    UUID parcelId = UUID.fromString(source.readString());
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
        return id.toString().compareTo(o.text.toString());
    }
}
