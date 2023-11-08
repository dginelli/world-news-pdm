package it.unimib.worldnews.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Class to represent the news of NewsAPI.org (<a href="https://newsapi.org">...</a>)
 */
public class News implements Parcelable {
    private String author;
    private String title;
    private NewsSource source;
    private String description;
    private String url;
    private String urlToImage;
    @SerializedName("publishedAt")
    private String date;
    private String content;

    public News() {}

    public News(String author, String title, NewsSource source, String description, String url,
                String urlToImage, String date, String content) {
        this.author = author;
        this.title = title;
        this.source = source;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.date = date;
        this.content = content;
    }

    public News(String author, String title, NewsSource source, String date) {
        this(author, title, source, null, null, null, date, null);
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public NewsSource getSource() {
        return source;
    }

    public void setSource(NewsSource source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "News{" +
                "author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", source=" + source +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                ", urlToImage='" + urlToImage + '\'' +
                ", date='" + date + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.author);
        dest.writeString(this.title);
        dest.writeParcelable(this.source, flags);
        dest.writeString(this.description);
        dest.writeString(this.url);
        dest.writeString(this.urlToImage);
        dest.writeString(this.date);
        dest.writeString(this.content);
    }

    public void readFromParcel(Parcel source) {
        this.author = source.readString();
        this.title = source.readString();
        this.source = source.readParcelable(NewsSource.class.getClassLoader());
        this.description = source.readString();
        this.url = source.readString();
        this.urlToImage = source.readString();
        this.date = source.readString();
        this.content = source.readString();
    }

    protected News(Parcel in) {
        this.author = in.readString();
        this.title = in.readString();
        this.source = in.readParcelable(NewsSource.class.getClassLoader());
        this.description = in.readString();
        this.url = in.readString();
        this.urlToImage = in.readString();
        this.date = in.readString();
        this.content = in.readString();
    }

    public static final Parcelable.Creator<News> CREATOR = new Parcelable.Creator<News>() {
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