package com.deankennedy.llmenhancedlearningapp.model;

public class TaskGenerationResponse {

    private String topic, lessonSummary;

    private String question1, answer1A, answer1B, answer1C, correctAnswer1;
    private String question2, answer2A, answer2B, answer2C, correctAnswer2;

    public String getTopic() {
        return topic;
    }

    public String getLessonSummary() {
        return lessonSummary;
    }

    public String getQuestion1() {
        return question1;
    }

    public String getAnswer1A() {
        return answer1A;
    }

    public String getAnswer1B() {
        return answer1B;
    }

    public String getAnswer1C() {
        return answer1C;
    }

    public String getCorrectAnswer1() {
        return correctAnswer1;
    }

    public String getQuestion2() {
        return question2;
    }

    public String getAnswer2A() {
        return answer2A;
    }

    public String getAnswer2B() {
        return answer2B;
    }

    public String getAnswer2C() {
        return answer2C;
    }

    public String getCorrectAnswer2() {
        return correctAnswer2;
    }
}
