package backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(
        @NotNull(message = "Transaction date is required")
        LocalDate transactionDate,

        @NotBlank(message = "Account number is required")
        String accountNumber,

        @NotBlank(message = "Account holder name is required")
        String accountHolderName,

        @NotNull(message = "Amount is required")
        BigDecimal amount
) {
}