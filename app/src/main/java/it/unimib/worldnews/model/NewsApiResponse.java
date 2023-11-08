package it.unimib.worldnews.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Class to represent the API response of NewsAPI.org (https://newsapi.org)
 * associated with the endpoint "Top headlines" - /v2/top-headlines.
 */
public class NewsApiResponse implements Parcelable {
    private String status;
    private int totalResults;
    private List<News> articles;

    public NewsApiResponse() {}

    public NewsApiResponse(String status, int totalResults, List<News> articles) {
        this.status = status;
        this.totalResults = totalResults;
        this.articles = articles;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<News> getArticles() {
        return articles;
    }

    public void setArticles(List<News> articles) {
        this.articles = articles;
    }

    @Override
    public String toString() {
        return "NewsApiResponse{" +
                "status='" + status + '\'' +
                ", totalResults=" + totalResults +
                ", articles=" + articles +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.status);
        dest.writeInt(this.totalResults);
        dest.writeTypedList(this.articles);
    }

    public void readFromParcel(Parcel source) {
        this.status = source.readString();
        this.totalResults = source.readInt();
        this.articles = source.createTypedArrayList(News.CREATOR);
    }

    protected NewsApiResponse(Parcel in) {
        this.status = in.readString();
        this.totalResults = in.readInt();
        this.articles = in.createTypedArrayList(News.CREATOR);
    }

    public static final Parcelable.Creator<NewsApiResponse> CREATOR = new Parcelable.Creator<NewsApiResponse>() {
        @Override
        public NewsApiResponse createFromParcel(Parcel source) {
            return new NewsApiResponse(source);
        }

        @Override
        public NewsApiResponse[] newArray(int size) {
            return new NewsApiResponse[size];
        }
    };
}
