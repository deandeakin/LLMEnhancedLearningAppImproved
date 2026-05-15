package com.deankennedy.llmenhancedlearningapp.model;

import java.util.ArrayList;

public class TaskGenerationRequest {

    private String username;
    private ArrayList<String> interests;

    public TaskGenerationRequest(String username, ArrayList<String> interests) {
        this.username = username;
        this.interests = interests;
    }

    public String getUsername() {
        return username;
    }

    public ArrayList<String> getInterests() {
        return interests;
    }
}