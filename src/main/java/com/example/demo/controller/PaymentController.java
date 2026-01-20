package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final BookingService bookingService;

    // Lấy giá trị từ application.properties
    @Value("${app.backend.url}")
    private String backendUrl;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    // 1. API TẠO URL THANH TOÁN
    @GetMapping("/create-payment-url")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(@RequestParam Long bookingId) {
        // SỬA: Dùng backendUrl thay vì localhost cứng
        // Link này sẽ là: https://badminton-api.onrender.com/api/payment/vnpay-return...
        String mockUrl = backendUrl + "/api/payment/vnpay-return?vnp_TxnRef=" + bookingId + "&vnp_ResponseCode=00";

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Tạo URL thanh toán thành công")
                .data(mockUrl) 
                .build());
    }

    // 2. API XỬ LÝ KẾT QUẢ VÀ CHUYỂN HƯỚNG
    @GetMapping("/vnpay-return")
    public RedirectView vnpayReturn(
            @RequestParam("vnp_TxnRef") String bookingIdStr,
            @RequestParam("vnp_ResponseCode") String responseCode
    ) {
        log.info("VNPay callback: bookingId={}, responseCode={}", bookingIdStr, responseCode);

        if ("00".equals(responseCode)) {
            try {
                Long bookingId = Long.parseLong(bookingIdStr);
                
                // Cập nhật trạng thái đơn hàng trong DB
                bookingService.confirmBookingPayment(bookingId);
                
                // SỬA: Chuyển hướng về trang Frontend trên Vercel
                return new RedirectView(frontendUrl + "/profile?payment=success");
                
            } catch (Exception e) {
                log.error("Lỗi xử lý thanh toán: ", e);
                // SỬA: Chuyển hướng về trang lỗi Frontend
                return new RedirectView(frontendUrl + "/profile?payment=error");
            }
        } else {
            // Thanh toán thất bại -> Quay về báo lỗi Frontend
            return new RedirectView(frontendUrl + "/profile?payment=failed");
        }
    }

    // 3. API XÁC NHẬN CHUYỂN KHOẢN (Cho Modal QR)
    @PostMapping("/confirm-transfer")
    public ResponseEntity<ApiResponse<String>> confirmTransfer(@RequestParam Long bookingId) {
        // Chuyển sang trạng thái WAITING (Chờ duyệt)
        bookingService.requestPaymentConfirmation(bookingId);
        
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Đã gửi yêu cầu xác nhận thanh toán")
                .data("WAITING")
                .build());
    }
}