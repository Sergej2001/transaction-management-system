package backend.controller;

import backend.exceptions.GlobalExceptionHandler;
import backend.repository.TransactionRepository;
import backend.service.TransactionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isIn;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransactionControllerTest {

    private static final String HEADER =
            "Transaction Date,Account Number,Account Holder Name,Amount,Status";

    private static final String VALID_REQUEST = """
            {
              "transactionDate": "2025-03-01",
              "accountNumber": "0123-4567-8901",
              "accountHolderName": "Sergej Pajović",
              "amount": 150.25
            }
            """;

    @TempDir
    Path temporaryDirectory;

    private Path csvFile;
    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() throws Exception {
        csvFile = temporaryDirectory.resolve("transactions.csv");

        Files.writeString(
                csvFile,
                HEADER + "\n",
                StandardCharsets.UTF_8
        );

        var repository = new TransactionRepository(csvFile.toString());
        var service = new TransactionService(repository);
        var controller = new TransactionController(service);

        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        if (validator != null) {
            validator.close();
        }
    }

    @Test
    void returnsEmptyArrayWhenThereAreNoTransactions() throws Exception {
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_JSON
                ))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void returnsTransactionsFromCsv() throws Exception {
        Files.writeString(
                csvFile,
                HEADER + "\n"
                        + "2025-03-01,0123-4567-8901,"
                        + "Maria Johnson,150.25,Settled\n",
                StandardCharsets.UTF_8
        );

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].transactionDate")
                        .value("2025-03-01"))
                .andExpect(jsonPath("$[0].accountNumber")
                        .value("0123-4567-8901"))
                .andExpect(jsonPath("$[0].accountHolderName")
                        .value("Maria Johnson"))
                .andExpect(jsonPath("$[0].amount")
                        .value(150.25))
                .andExpect(jsonPath("$[0].status")
                        .value("Settled"));
    }

    @Test
    void createsTransactionAndPersistsAssignedStatus() throws Exception {
        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding(StandardCharsets.UTF_8.name())
                        .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.transactionDate")
                        .value("2025-03-01"))
                .andExpect(jsonPath("$.accountNumber")
                        .value("0123-4567-8901"))
                .andExpect(jsonPath("$.accountHolderName")
                        .value("Sergej Pajović"))
                .andExpect(jsonPath("$.amount")
                        .value(150.25))
                .andExpect(jsonPath("$.status", isIn(
                        new String[]{"Pending", "Settled", "Failed"}
                )));

        var reopenedRepository =
                new TransactionRepository(csvFile.toString());

        var savedTransactions = reopenedRepository.findAll();

        assertEquals(1, savedTransactions.size());

        var saved = savedTransactions.get(0);

        assertEquals("Sergej Pajović", saved.accountHolderName());

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status")
                        .value(saved.status().name()));
    }

    @Test
    void rejectsInvalidFieldsWithoutChangingCsv() throws Exception {
        String originalContent = Files.readString(csvFile);

        String invalidRequest = """
                {
                  "transactionDate": "9999-12-31",
                  "accountNumber": "abc",
                  "accountHolderName": "123123",
                  "amount": -123.00
                }
                """;

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Please check the entered data."))
                .andExpect(jsonPath("$.fieldErrors.transactionDate")
                        .isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.accountNumber")
                        .isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.accountHolderName")
                        .isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.amount")
                        .isNotEmpty());

        assertEquals(originalContent, Files.readString(csvFile));
    }

    @Test
    void rejectsMissingFieldsWithoutChangingCsv() throws Exception {
        String originalContent = Files.readString(csvFile);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.transactionDate")
                        .isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.accountNumber")
                        .isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.accountHolderName")
                        .isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.amount")
                        .isNotEmpty());

        assertEquals(originalContent, Files.readString(csvFile));
    }

    @Test
    void rejectsMalformedJsonWithoutChangingCsv() throws Exception {
        String originalContent = Files.readString(csvFile);

        mockMvc.perform(post("/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());

        assertEquals(originalContent, Files.readString(csvFile));
    }

    @Test
    void returnsServerErrorWhenCsvIsMissing() throws Exception {
        Files.delete(csvFile);

        mockMvc.perform(get("/transactions"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value(
                        "Unable to access transaction data. Please try again later."
                ));
    }
}