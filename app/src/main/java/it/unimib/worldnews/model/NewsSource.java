package it.unimib.worldnews.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Class to represent the source of news of NewsAPI.org (https://newsapi.org).
 */
public class NewsSource implements Parcelable {
    private String name;

    public NewsSource() {
    }

    public NewsSource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsSource that = (NewsSource) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "NewsSource{" +
                "name='" + name + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
    }

    public void readFromParcel(Parcel source) {
        this.name = source.readString();
    }

    protected NewsSource(Parcel in) {
        this.name = in.readString();
    }

    public static final Creator<NewsSource> CREATOR = new Creator<NewsSource>() {
        @Override
        public NewsSource createFromParcel(Parcel source) {
            return new NewsSource(source);
        }

        @Override
        public NewsSource[] newArray(int size) {
            return new NewsSource[size];
        }
    };
}
