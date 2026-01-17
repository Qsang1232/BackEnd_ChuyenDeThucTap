package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.CourtRequest; // Nhớ import cái này
import com.example.demo.model.Court;
import com.example.demo.service.CourtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courts")
@RequiredArgsConstructor
public class CourtController {

    private final CourtService courtService;

    // 1. Lấy tất cả sân (Không gán ảnh cứng nữa)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Court>>> getAllCourts() {
        List<Court> courts = courtService.getAllCourts();
        
        return ResponseEntity.ok(ApiResponse.<List<Court>>builder()
                .success(true)
                .message("Lấy danh sách sân thành công")
                .data(courts)
                .build());
    }

    // 2. Lấy chi tiết
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Court>> getCourtById(@PathVariable Long id) {
        Court court = courtService.getCourtById(id);

        return ResponseEntity.ok(ApiResponse.<Court>builder()
                .success(true)
                .message("Tìm thấy sân thành công")
                .data(court)
                .build());
    }

    // 3. Tạo mới sân (Dùng CourtRequest) - Đã sửa lỗi gọi sai hàm
    @PostMapping
    public ResponseEntity<ApiResponse<Court>> createCourt(@RequestBody CourtRequest request) {
        // Gọi hàm addCourt(CourtRequest) của Service
        Court newCourt = courtService.addCourt(request);
        
        return new ResponseEntity<>(ApiResponse.<Court>builder()
                .success(true)
                .message("Tạo sân mới thành công")
                .data(newCourt)
                .build(), HttpStatus.CREATED);
    }

    // 4. Cập nhật sân (Dùng CourtRequest) - Đã sửa lỗi gọi sai hàm
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Court>> updateCourt(@PathVariable Long id, @RequestBody CourtRequest request) {
        // Gọi hàm updateCourt(Long, CourtRequest) của Service
        Court updatedCourt = courtService.updateCourt(id, request);
        
        return ResponseEntity.ok(ApiResponse.<Court>builder()
                .success(true)
                .message("Cập nhật thông tin sân thành công")
                .data(updatedCourt)
                .build());
    }

    // 5. Xóa sân
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourt(@PathVariable Long id) {
        courtService.deleteCourt(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Xóa sân thành công")
                .build());
    }
}