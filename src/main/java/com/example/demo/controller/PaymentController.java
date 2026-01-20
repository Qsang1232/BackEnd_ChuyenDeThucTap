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

    // Backend & Frontend URL (có default để không crash)
    @Value("${app.backend.url:http://localhost:8080}")
    private String backendUrl;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    // 1. API TẠO URL THANH TOÁN
    @GetMapping("/create-payment-url")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(
            @RequestParam Long bookingId
    ) {
        // Mock URL giả lập VNPay callback
        String mockUrl = backendUrl
                + "/api/payment/vnpay-return"
                + "?vnp_TxnRef=" + bookingId
                + "&vnp_ResponseCode=00";

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Tạo URL thanh toán thành công")
                        .data(mockUrl)
                        .build()
        );
    }

    // 2. API NHẬN CALLBACK TỪ VNPay & REDIRECT VỀ FRONTEND
    @GetMapping("/vnpay-return")
    public RedirectView vnpayReturn(
            @RequestParam("vnp_TxnRef") String bookingIdStr,
            @RequestParam("vnp_ResponseCode") String responseCode
    ) {
        log.info("VNPay callback: bookingId={}, responseCode={}", bookingIdStr, responseCode);

        try {
            Long bookingId = Long.parseLong(bookingIdStr);

            if ("00".equals(responseCode)) {
                // Thanh toán thành công
                bookingService.confirmBookingPayment(bookingId);
                return new RedirectView(frontendUrl + "/profile?payment=success");
            } else {
                // Thanh toán thất bại
                return new RedirectView(frontendUrl + "/profile?payment=failed");
            }

        } catch (Exception e) {
            log.error("Lỗi xử lý callback thanh toán", e);
            return new RedirectView(frontendUrl + "/profile?payment=error");
        }
    }

    // 3. API XÁC NHẬN CHUYỂN KHOẢN (QR / Manual)
    @PostMapping("/confirm-transfer")
    public ResponseEntity<ApiResponse<String>> confirmTransfer(
            @RequestParam Long bookingId
    ) {
        bookingService.requestPaymentConfirmation(bookingId);

        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Đã gửi yêu cầu xác nhận thanh toán")
                        .data("WAITING")
                        .build()
        );
    }
}
