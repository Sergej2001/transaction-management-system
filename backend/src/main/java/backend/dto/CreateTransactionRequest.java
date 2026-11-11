package backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransactionRequest(

        @NotNull(message = "Transaction date is required")
        @PastOrPresent(
                message = "Transaction date cannot be in the future"
        )
        LocalDate transactionDate,

        @NotBlank(message = "Account number is required")
        @Pattern(
                regexp = "[0-9]{4}-[0-9]{4}-[0-9]{4}",
                message = "Use the format 1234-5678-9012"
        )
        String accountNumber,

        @NotBlank(message = "Account holder name is required")
        @Size(
                max = 100,
                message = "Name must contain at most 100 characters"
        )
        @Pattern(
                regexp = "(?s)(?=.*\\p{L})[\\p{L}\\p{M} .,'’\\-]+",
                message = "Use letters, spaces, periods, commas, apostrophes or hyphens"
        )
        String accountHolderName,

        @NotNull(message = "Amount is required")
        @DecimalMin(
                value = "0.00",
                inclusive = false,
                message = "Amount must be greater than zero"
        )
        @Digits(
                integer = 9,
                fraction = 2,
                message = "Use at most 9 whole-number digits and 2 decimal places"
        )
        BigDecimal amount

) {

    public CreateTransactionRequest {
        accountNumber = accountNumber == null
                ? null
                : accountNumber.strip();

        accountHolderName = accountHolderName == null
                ? null
                : accountHolderName.strip();
    }
}