package it.unimib.worldnews.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class to represent news.
 */
public class News implements Parcelable {
    private String title;
    private String author;
    private String source;
    private String date;

    public News(String title, String author, String source, String date) {
        this.title = title;
        this.author = author;
        this.source = source;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", source='" + source + '\'' +
                ", date='" + date + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeString(this.source);
        dest.writeString(this.date);
    }

    public void readFromParcel(Parcel source) {
        this.title = source.readString();
        this.author = source.readString();
        this.source = source.readString();
        this.date = source.readString();
    }

    protected News(Parcel in) {
        this.title = in.readString();
        this.author = in.readString();
        this.source = in.readString();
        this.date = in.readString();
    }

    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel source) {
            return new News(source);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };
}
