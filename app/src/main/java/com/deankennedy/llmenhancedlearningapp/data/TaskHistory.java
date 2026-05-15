package com.deankennedy.llmenhancedlearningapp.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_history")
public class TaskHistory {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String username, topic, question, selectedAnswer, correctAnswer, utilityUsed, prompt, response;
    private long timestamp;

    public TaskHistory(String username, String topic, String question, String selectedAnswer, String correctAnswer, String utilityUsed, String prompt, String response, long timestamp) {
        this.username = username;
        this.topic = topic;
        this.question = question;
        this.selectedAnswer = selectedAnswer;
        this.correctAnswer = correctAnswer;
        this.utilityUsed = utilityUsed;
        this.prompt = prompt;
        this.response = response;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getTopic() {
        return topic;
    }

    public String getQuestion() {
        return question;
    }

    public String getSelectedAnswer() {
        return selectedAnswer;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public String getUtilityUsed() {
        return utilityUsed;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getResponse() {
        return response;
    }

    public long getTimestamp() {
        return timestamp;
    }
}