package com.example.demo.service;

import com.example.demo.dto.ReviewRequest;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Booking;
import com.example.demo.model.Review;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    // Lấy danh sách đánh giá của 1 sân (Cho trang chi tiết sân)
    public List<Review> getReviewsForCourt(Long courtId) {
        return reviewRepository.findByCourtId(courtId);
    }

    // Tạo đánh giá mới (Dựa trên Booking ID để đảm bảo đã đặt sân mới được đánh giá)
    @Transactional
    public Review createReview(ReviewRequest request, String username) {
        // 1. Tìm User đang đăng nhập
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // 2. Tìm đơn đặt sân (Booking)
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt với ID: " + request.getBookingId()));

        // 3. Kiểm tra quyền: Chỉ người đặt đơn này mới được đánh giá
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng của người khác!");
        }

        // 4. (Tùy chọn) Kiểm tra xem đơn đã hoàn thành chưa?
        // if (!"COMPLETED".equals(booking.getStatus()) && !"CONFIRMED".equals(booking.getStatus())) {
        //    throw new RuntimeException("Bạn chỉ được đánh giá khi đã hoàn thành buổi chơi.");
        // }

        // 5. Lưu đánh giá
        Review review = Review.builder()
                .user(user)
                .court(booking.getCourt()) // Lấy thông tin sân từ đơn đặt
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        return reviewRepository.save(review);
    }
}