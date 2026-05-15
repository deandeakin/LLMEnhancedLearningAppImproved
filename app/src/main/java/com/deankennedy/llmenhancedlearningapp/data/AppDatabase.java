package com.deankennedy.llmenhancedlearningapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.deankennedy.llmenhancedlearningapp.data.TaskHistoryDao;
import com.deankennedy.llmenhancedlearningapp.data.TaskHistory;

@Database(entities = {TaskHistory.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract TaskHistoryDao taskHistoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, "learning_app_database").allowMainThreadQueries().build();
        }
        return instance;
    }
}
