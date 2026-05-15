package com.deankennedy.llmenhancedlearningapp.network;

import com.deankennedy.llmenhancedlearningapp.model.LearningRequest;
import com.deankennedy.llmenhancedlearningapp.model.LearningResponse;
import com.deankennedy.llmenhancedlearningapp.model.TaskGenerationRequest;
import com.deankennedy.llmenhancedlearningapp.model.TaskGenerationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("learning-assistant")
    Call<LearningResponse> getLearningFeedback(@Body LearningRequest request);

    @POST("generate-task")
    Call<TaskGenerationResponse> generateTask(@Body TaskGenerationRequest request);
}
