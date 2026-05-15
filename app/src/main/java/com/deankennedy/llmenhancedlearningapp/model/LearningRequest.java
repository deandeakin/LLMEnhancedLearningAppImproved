package com.deankennedy.llmenhancedlearningapp.model;

public class LearningRequest {

    private String username, topic, lessonSummary, question, selectedAnswer, correctAnswer, utility;

    public LearningRequest(String username, String topic, String lessonSummary, String question, String selectedAnswer, String correctAnswer, String utility) {
        this.username = username;
        this.topic = topic;
        this.lessonSummary = lessonSummary;
        this.question = question;
        this.selectedAnswer = selectedAnswer;
        this.correctAnswer = correctAnswer;
        this.utility = utility;
    }

    public String getUsername() {
        return username;
    }

    public String getTopic() {
        return topic;
    }

    public String getLessonSummary() {
        return lessonSummary;
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

    public String getUtility() {
        return utility;
    }
}
