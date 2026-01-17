package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.BookingRequest;
import com.example.demo.dto.BookingResponse; // Nhớ import DTO
import com.example.demo.model.Booking;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // Helper: Chuyển đổi dữ liệu để Frontend hiển thị tên đẹp
    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .courtId(booking.getCourt().getId())
                .courtName(booking.getCourt().getName())
                .courtImage(booking.getCourt().getImageUrl())
                // QUAN TRỌNG: Lấy tên user để hiển thị bảng Admin
                .username(booking.getUser().getUsername()) 
                .userId(booking.getUser().getId())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .totalPrice(booking.getTotalPrice())
                .status(booking.getStatus())
                .build();
    }

    // 1. Tạo đơn
    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@RequestBody BookingRequest request) {
        Booking newBooking = bookingService.createBooking(
                request.getCourtId(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok(ApiResponse.<BookingResponse>builder()
                .success(true).message("Đặt sân thành công!").data(mapToResponse(newBooking)).build());
    }

    // 2. Lịch sử của tôi
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyHistory() {
        List<BookingResponse> list = bookingService.getMyBookings().stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder().success(true).data(list).build());
    }

    // 3. Admin xem tất cả (Sửa để hiện tên khách)
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getAllBookings() {
        List<BookingResponse> list = bookingService.getAllBookings().stream().map(this::mapToResponse).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<BookingResponse>>builder().success(true).data(list).build());
    }

    // 4. Hủy đơn
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Hủy thành công").build());
    }

    // 5. Duyệt đơn (CONFIRM) - QUAN TRỌNG
    @PutMapping("/{id}/confirm")
    public ResponseEntity<ApiResponse<Void>> confirmBooking(@PathVariable Long id) {
        bookingService.confirmBooking(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).message("Đã duyệt đơn").build());
    }
}