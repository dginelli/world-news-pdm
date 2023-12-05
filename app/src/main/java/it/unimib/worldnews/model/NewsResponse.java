package it.unimib.worldnews.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class NewsResponse implements Parcelable {

    @SerializedName("articles")
    private List<News> newsList;

    public NewsResponse() {}

    public NewsResponse(List<News> newsList) {
        this.newsList = newsList;
    }

    public List<News> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<News> newsList) {
        this.newsList = newsList;
    }

    @Override
    public String toString() {
        return "NewsResponse{" +
                "newsList=" + newsList +
                '}';
    }

    public static final Creator<NewsResponse> CREATOR = new Creator<NewsResponse>() {
        @Override
        public NewsResponse createFromParcel(Parcel in) {
            return new NewsResponse(in);
        }

        @Override
        public NewsResponse[] newArray(int size) {
            return new NewsResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.newsList);
    }

    public void readFromParcel(Parcel source) {
        this.newsList = source.createTypedArrayList(News.CREATOR);
    }

    protected NewsResponse(Parcel in) {
        this.newsList = in.createTypedArrayList(News.CREATOR);
    }
}
