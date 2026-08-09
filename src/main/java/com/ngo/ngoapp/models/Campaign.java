package com.ngo.ngoapp.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "campaigns")
public class Campaign {
    @Id
    private String id;
    private String title;

    private String description;

    private Double targetAmount;
    private Double raisedAmount = 0.0;

    private String imageUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Campaign() {}

    public Campaign(String id, String title, String description, Double targetAmount, Double raisedAmount, String imageUrl, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.targetAmount = targetAmount;
        this.raisedAmount = raisedAmount;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(Double targetAmount) { this.targetAmount = targetAmount; }
    public Double getRaisedAmount() { return raisedAmount; }
    public void setRaisedAmount(Double raisedAmount) { this.raisedAmount = raisedAmount; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
