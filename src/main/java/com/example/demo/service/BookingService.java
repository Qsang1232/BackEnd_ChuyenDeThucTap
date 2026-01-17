package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Booking;
import com.example.demo.model.Court;
import com.example.demo.model.User;
import com.example.demo.repository.BookingRepository;
import com.example.demo.repository.CourtRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CourtRepository courtRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tồn tại"));
    }

    @Transactional
    public Booking createBooking(Long courtId, LocalDateTime startTime, LocalDateTime endTime) {
        User user = getCurrentUser();
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sân với ID: " + courtId));

        // 1. Validate thời gian
        if (startTime.isAfter(endTime) || startTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Thời gian đặt không hợp lệ!");
        }

        // 2. Check trùng
        boolean isConflict = bookingRepository.existsConflictingBooking(courtId, startTime, endTime);
        if (isConflict) {
            throw new RuntimeException("Khung giờ này đã có người đặt rồi! Vui lòng chọn giờ khác.");
        }

        // 3. Tính tiền
        long minutes = Duration.between(startTime, endTime).toMinutes();
        BigDecimal hours = new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
        if (hours.compareTo(BigDecimal.ONE) < 0) hours = BigDecimal.ONE;

        BigDecimal totalPrice = hours.multiply(court.getPricePerHour());

        // 4. Tạo Booking (Dùng Builder chuẩn)
        Booking booking = Booking.builder()
                .user(user)
                .court(court)
                .startTime(startTime)
                .endTime(endTime)
                .totalPrice(totalPrice)
                .status("PENDING")
                .build();

        return bookingRepository.save(booking);
    }

    public List<Booking> getMyBookings() {
        return bookingRepository.findByUserId(getCurrentUser().getId());
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

 @Transactional
    public void cancelBooking(Long id) {
        User currentUser = getCurrentUser();
        Booking booking = bookingRepository.findById(id)
             .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn với ID: " + id));

        // 1. Kiểm tra xem có phải chủ đơn không
        boolean isOwner = booking.getUser().getId().equals(currentUser.getId());
        
        // 2. Kiểm tra xem có phải Admin không (Cách an toàn nhất)
        String currentRole = String.valueOf(currentUser.getRole()).toUpperCase();
        
        // In ra để debug (Nhìn vào cửa sổ chạy Backend sẽ thấy dòng này)
        System.out.println(">>> DEBUG: User đang hủy là: " + currentUser.getUsername() + " | Role: " + currentRole);

        boolean isAdmin = currentRole.contains("ADMIN"); // Chấp nhận cả "ADMIN" và "ROLE_ADMIN"

        // Nếu không phải Chủ đơn VÀ không phải Admin -> Chặn
        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Bạn không có quyền hủy đơn này. (Role của bạn là: " + currentRole + ")");
        }

        // 3. Logic chặn thời gian:
        // CHỈ ÁP DỤNG NẾU LÀ USER THƯỜNG (Admin thì bỏ qua bước này)
        if (!isAdmin) {
            // Nếu đơn đã Hoàn thành hoặc đã Hủy -> User không được đụng vào nữa
            if ("CANCELLED".equals(booking.getStatus()) || "COMPLETED".equals(booking.getStatus())) {
                 throw new RuntimeException("Đơn này không thể hủy được nữa.");
            }
            
            // User thường: Phải hủy trước 2 tiếng
            // LocalDateTime now = LocalDateTime.now();
            // if (booking.getStartTime().isBefore(now.plusHours(2))) {
            //    throw new RuntimeException("Chỉ được hủy trước giờ chơi 2 tiếng!");
            // }
        }

        booking.setStatus("CANCELLED");
        bookingRepository.save(booking);
    }

    @Transactional
    public void confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn với ID: " + id));
        booking.setStatus("CONFIRMED");
        bookingRepository.save(booking);
    }
     // User bấm "Đã chuyển khoản" -> Gọi hàm này
    @Transactional
    public void requestPaymentConfirmation(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));
        
        // Đặt trạng thái mới: PAID_WAITING
        // Lưu ý: Nếu cột status trong DB ngắn quá thì đặt là "WAITING" thôi
        booking.setStatus("WAITING"); 
        bookingRepository.save(booking);
    }
    @Transactional
    public void confirmBookingPayment(Long id) {
        confirmBooking(id);
    }

    public List<Booking> getBookingsByDate(Long courtId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        return bookingRepository.findBookingsByCourtAndDate(courtId, startOfDay, endOfDay);
    }
}