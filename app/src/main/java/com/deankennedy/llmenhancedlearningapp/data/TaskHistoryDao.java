package com.deankennedy.llmenhancedlearningapp.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.deankennedy.llmenhancedlearningapp.data.TaskHistory;

import java.util.List;

@Dao
public interface TaskHistoryDao {

    @Insert
    void insert(TaskHistory taskHistory);

    @Query("SELECT * FROM task_history ORDER BY timestamp DESC")
    List<TaskHistory> getAllHistory();

    @Query("SELECT * FROM task_history WHERE username = :username ORDER BY timestamp DESC")
    List<TaskHistory> getHistoryForUser(String username);

    @Query("SELECT * FROM task_history WHERE username = :username ORDER BY timestamp DESC LIMIT 1")
    TaskHistory getLatestHistoryForUser(String username);
}
