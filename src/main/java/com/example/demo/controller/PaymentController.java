package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView; // <--- Import quan trọng

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final BookingService bookingService;

    // 1. API TẠO URL THANH TOÁN (Frontend gọi cái này để lấy link)
    @GetMapping("/create-payment-url")
    public ResponseEntity<ApiResponse<String>> createPaymentUrl(@RequestParam Long bookingId) {
        // Tạo link giả lập gọi lại chính server mình
        String mockUrl = "http://localhost:8080/api/payment/vnpay-return?vnp_TxnRef=" + bookingId + "&vnp_ResponseCode=00";

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Tạo URL thanh toán thành công")
                .data(mockUrl) 
                .build());
    }

    // 2. API XỬ LÝ KẾT QUẢ VÀ CHUYỂN HƯỚNG
    // Thay vì trả về JSON, ta dùng RedirectView để đẩy người dùng về lại Frontend
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
                
                // Chuyển hướng về trang Lịch sử của React (kèm tham số success)
                return new RedirectView("http://localhost:3000/profile?payment=success");
                
            } catch (Exception e) {
                log.error("Lỗi xử lý thanh toán: ", e);
                return new RedirectView("http://localhost:3000/profile?payment=error");
            }
        } else {
            // Thanh toán thất bại -> Quay về báo lỗi
            return new RedirectView("http://localhost:3000/profile?payment=failed");
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