package backend.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CreateTransactionRequestTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void acceptsValidTransaction() {
        var request = validRequest(new BigDecimal("150.25"));

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsUnicodeNamesAndTrimsSurroundingSpaces() {
        var request = new CreateTransactionRequest(
                LocalDate.now().minusDays(1),
                " 0123-4567-8901 ",
                " Željko O’Connor-Pajović ",
                new BigDecimal("75.50")
        );

        assertTrue(validator.validate(request).isEmpty());
        assertEquals("0123-4567-8901", request.accountNumber());
        assertEquals("Željko O’Connor-Pajović", request.accountHolderName());
    }

    @Test
    void acceptsToday() {
        var request = new CreateTransactionRequest(
                LocalDate.now(),
                "0123-4567-8901",
                "Sergej Pajović",
                new BigDecimal("10.00")
        );

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsFutureDate() {
        var request = new CreateTransactionRequest(
                LocalDate.now().plusDays(1),
                "0123-4567-8901",
                "Sergej Pajović",
                new BigDecimal("10.00")
        );

        assertEquals(
                Set.of("transactionDate"),
                invalidFields(request)
        );
    }

    @Test
    void rejectsInvalidAccountNumbers() {
        for (String account : new String[]{
                "abcd-1234-5678",
                "123456789012",
                "1234-5678-901",
                "   "
        }) {
            var request = new CreateTransactionRequest(
                    LocalDate.now().minusDays(1),
                    account,
                    "Sergej Pajović",
                    new BigDecimal("10.00")
            );

            assertEquals(
                    Set.of("accountNumber"),
                    invalidFields(request),
                    "Should reject account: " + account
            );
        }
    }

    @Test
    void rejectsInvalidNames() {
        for (String name : new String[]{
                "",
                "   ",
                "123123",
                "Sergej123",
                "---",
                "A".repeat(101)
        }) {
            var request = new CreateTransactionRequest(
                    LocalDate.now().minusDays(1),
                    "0123-4567-8901",
                    name,
                    new BigDecimal("10.00")
            );

            assertEquals(
                    Set.of("accountHolderName"),
                    invalidFields(request),
                    "Should reject name: " + name
            );
        }
    }

    @Test
    void rejectsInvalidAmounts() {
        for (String amount : new String[]{
                "0",
                "-123.00",
                "10.001",
                "1000000000.00"
        }) {
            var request = validRequest(new BigDecimal(amount));

            assertEquals(
                    Set.of("amount"),
                    invalidFields(request),
                    "Should reject amount: " + amount
            );
        }
    }

    @Test
    void acceptsAmountBoundaries() {
        for (String amount : new String[]{
                "0.01",
                "999999999.99"
        }) {
            var request = validRequest(new BigDecimal(amount));

            assertTrue(
                    validator.validate(request).isEmpty(),
                    "Should accept amount: " + amount
            );
        }
    }

    @Test
    void rejectsMissingFields() {
        var request = new CreateTransactionRequest(
                null,
                null,
                null,
                null
        );

        assertEquals(
                Set.of(
                        "transactionDate",
                        "accountNumber",
                        "accountHolderName",
                        "amount"
                ),
                invalidFields(request)
        );
    }

    private static CreateTransactionRequest validRequest(
            BigDecimal amount
    ) {
        return new CreateTransactionRequest(
                LocalDate.now().minusDays(1),
                "0123-4567-8901",
                "Sergej Pajović",
                amount
        );
    }

    private static Set<String> invalidFields(
            CreateTransactionRequest request
    ) {
        return validator.validate(request)
                .stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}