package it.unimib.worldnews.database;

import static it.unimib.worldnews.util.Constants.DATABASE_VERSION;
import static it.unimib.worldnews.util.Constants.NEWS_DATABASE_NAME;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.unimib.worldnews.model.News;

/**
 * Main access point for the underlying connection to the local database.
 * https://developer.android.com/reference/kotlin/androidx/room/Database
 */
@Database(entities = {News.class}, version = DATABASE_VERSION)
public abstract class NewsRoomDatabase extends RoomDatabase {

    public abstract NewsDao newsDao();

    private static volatile NewsRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static NewsRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NewsRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            NewsRoomDatabase.class, NEWS_DATABASE_NAME).build();
                }
            }
        }
        return INSTANCE;
    }
}
