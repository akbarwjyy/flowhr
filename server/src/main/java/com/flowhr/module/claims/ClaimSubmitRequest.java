package com.flowhr.module.claims;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class ClaimSubmitRequest {

    @NotNull(message = "Kategori klaim wajib diisi")
    private Long categoryId;

    @NotBlank(message = "Judul klaim wajib diisi")
    private String title;

    private String description;

    @NotNull(message = "Jumlah klaim wajib diisi")
    @DecimalMin(value = "0.01", message = "Jumlah harus lebih dari 0")
    private BigDecimal amount;

    @NotNull(message = "Tanggal klaim wajib diisi")
    private LocalDate claimDate;

    private String receiptUrl;
}
