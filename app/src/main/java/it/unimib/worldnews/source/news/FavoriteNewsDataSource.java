package it.unimib.worldnews.source.news;

import static it.unimib.worldnews.util.Constants.FIREBASE_FAVORITE_NEWS_COLLECTION;
import static it.unimib.worldnews.util.Constants.FIREBASE_REALTIME_DATABASE;
import static it.unimib.worldnews.util.Constants.FIREBASE_USERS_COLLECTION;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import it.unimib.worldnews.model.News;

/**
 * Class to get the user favorite news using Firebase Realtime Database.
 */
public class FavoriteNewsDataSource extends BaseFavoriteNewsDataSource {

    private static final String TAG = FavoriteNewsDataSource.class.getSimpleName();

    private final DatabaseReference databaseReference;
    private final String idToken;

    public FavoriteNewsDataSource(String idToken) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(FIREBASE_REALTIME_DATABASE);
        databaseReference = firebaseDatabase.getReference().getRef();
        this.idToken = idToken;
    }

    @Override
    public void getFavoriteNews() {
        databaseReference.child(FIREBASE_USERS_COLLECTION).child(idToken).
            child(FIREBASE_FAVORITE_NEWS_COLLECTION).get().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.d(TAG, "Error getting data", task.getException());
                }
                else {
                    Log.d(TAG, "Successful read: " + task.getResult().getValue());

                    List<News> newsList = new ArrayList<>();
                    for(DataSnapshot ds : task.getResult().getChildren()) {
                        News news = ds.getValue(News.class);
                        news.setSynchronized(true);
                        newsList.add(news);
                    }

                    newsCallback.onSuccessFromCloudReading(newsList);
                }
            });
    }

    @Override
    public void addFavoriteNews(News news) {
        databaseReference.child(FIREBASE_USERS_COLLECTION).child(idToken).
            child(FIREBASE_FAVORITE_NEWS_COLLECTION).child(String.valueOf(news.hashCode())).setValue(news)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    news.setSynchronized(true);
                    newsCallback.onSuccessFromCloudWriting(news);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    newsCallback.onFailureFromCloud(e);
                }
            });
    }

    @Override
    public void synchronizeFavoriteNews(List<News> notSynchronizedNewsList) {
        databaseReference.child(FIREBASE_USERS_COLLECTION).child(idToken).
            child(FIREBASE_FAVORITE_NEWS_COLLECTION).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    List<News> newsList = new ArrayList<>();
                    for (DataSnapshot ds : task.getResult().getChildren()) {
                        News news = ds.getValue(News.class);
                        news.setSynchronized(true);
                        newsList.add(news);
                    }

                    newsList.addAll(notSynchronizedNewsList);

                    for (News news : newsList) {
                        databaseReference.child(FIREBASE_USERS_COLLECTION).child(idToken).
                            child(FIREBASE_FAVORITE_NEWS_COLLECTION).
                            child(String.valueOf(news.hashCode())).setValue(news).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            news.setSynchronized(true);
                                        }
                                    }
                            );
                    }
                }
            });
    }

    @Override
    public void deleteFavoriteNews(News news) {
        databaseReference.child(FIREBASE_USERS_COLLECTION).child(idToken).
            child(FIREBASE_FAVORITE_NEWS_COLLECTION).child(String.valueOf(news.hashCode())).
            removeValue().addOnSuccessListener(aVoid -> {
                news.setSynchronized(false);
                newsCallback.onSuccessFromCloudWriting(news);
            }).addOnFailureListener(e -> {
                newsCallback.onFailureFromCloud(e);
            });
    }

    @Override
    public void deleteAllFavoriteNews() {
        databaseReference.child(FIREBASE_USERS_COLLECTION).child(idToken).
            child(FIREBASE_FAVORITE_NEWS_COLLECTION).removeValue().addOnSuccessListener(aVoid -> {
                newsCallback.onSuccessFromCloudWriting(null);
            }).addOnFailureListener(e -> {
                newsCallback.onFailureFromCloud(e);
            });
    }
}
