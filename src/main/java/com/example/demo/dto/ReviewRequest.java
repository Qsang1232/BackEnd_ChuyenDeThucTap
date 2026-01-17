package com.example.demo.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    // Sửa courtId thành bookingId
    private Long bookingId; 
    private int rating;
    private String comment;
}