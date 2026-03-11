package com.flowhr.module.attendance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LeaveRequest {

    @NotBlank(message = "Jenis cuti wajib diisi")
    private String leaveType; // ANNUAL, SICK, MATERNITY, PATERNITY, MARRIAGE, BEREAVEMENT, UNPAID

    @NotNull(message = "Tanggal mulai wajib diisi")
    private LocalDate startDate;

    @NotNull(message = "Tanggal selesai wajib diisi")
    private LocalDate endDate;

    @NotBlank(message = "Alasan cuti wajib diisi")
    private String reason;

    private String attachmentUrl;
}
