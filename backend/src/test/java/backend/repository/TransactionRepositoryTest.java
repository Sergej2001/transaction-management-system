package backend.repository;

import backend.model.Transaction;
import backend.model.TransactionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRepositoryTest {

    private static final String HEADER =
            "Transaction Date,Account Number,Account Holder Name,Amount,Status";

    @TempDir
    Path temporaryDirectory;

    private Path csvFile;
    private TransactionRepository repository;

    @BeforeEach
    void setUp() throws IOException {
        csvFile = temporaryDirectory.resolve("transactions.csv");
        Files.writeString(
                csvFile,
                HEADER + "\n",
                StandardCharsets.UTF_8
        );

        repository = new TransactionRepository(csvFile.toString());
    }

    @Test
    void returnsEmptyListWhenFileContainsOnlyHeader() throws IOException {
        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void readsExistingTransaction() throws IOException {
        Files.writeString(
                csvFile,
                HEADER + "\n"
                        + "2025-03-01,0123-4567-8901,"
                        + "Sergej Pajović,150.25,Settled\n",
                StandardCharsets.UTF_8
        );

        var expected = new Transaction(
                LocalDate.of(2025, 3, 1),
                "0123-4567-8901",
                "Sergej Pajović",
                new BigDecimal("150.25"),
                TransactionStatus.Settled
        );

        assertEquals(List.of(expected), repository.findAll());
    }

    @Test
    void savesTransactionAndReadsItFromNewRepository() throws IOException {
        var transaction = transaction(
                "Sergej Pajović",
                TransactionStatus.Pending
        );

        assertEquals(transaction, repository.save(transaction));

        var reopenedRepository =
                new TransactionRepository(csvFile.toString());

        assertEquals(
                List.of(transaction),
                reopenedRepository.findAll()
        );
    }

    @Test
    void preservesExistingTransactionsWhenAddingAnother() throws IOException {
        var first = transaction(
                "Sergej Pajović",
                TransactionStatus.Settled
        );

        var second = transaction(
                "Maria Johnson",
                TransactionStatus.Failed
        );

        repository.save(first);
        repository.save(second);

        assertEquals(
                List.of(first, second),
                repository.findAll()
        );
    }

    @Test
    void preservesUnicodeCommasAndQuotes() throws IOException {
        var transaction = transaction(
                "Pajović, Željko \"Žile\"",
                TransactionStatus.Pending
        );

        repository.save(transaction);

        assertEquals(
                List.of(transaction),
                repository.findAll()
        );
    }

    @Test
    void appendsCorrectlyWhenOriginalFileHasNoFinalNewline()
            throws IOException {

        Files.writeString(
                csvFile,
                HEADER + "\n"
                        + "2025-03-01,0123-4567-8901,"
                        + "Maria Johnson,150.25,Settled",
                StandardCharsets.UTF_8
        );

        var added = transaction(
                "Sergej Pajović",
                TransactionStatus.Pending
        );

        repository.save(added);

        var transactions = repository.findAll();

        assertEquals(2, transactions.size());
        assertEquals(
                "Maria Johnson",
                transactions.get(0).accountHolderName()
        );
        assertEquals(added, transactions.get(1));
    }

    @Test
    void throwsExceptionWhenFileDoesNotExist() throws IOException {
        Files.delete(csvFile);

        assertThrows(IOException.class, () -> repository.findAll());
    }

    @Test
    void rejectsInvalidHeaders() throws IOException {
        Files.writeString(
                csvFile,
                "Date,Account,Name,Amount,Status\n",
                StandardCharsets.UTF_8
        );

        assertThrows(IOException.class, () -> repository.findAll());
    }

    @Test
    void rejectsRowWithMissingColumn() throws IOException {
        Files.writeString(
                csvFile,
                HEADER + "\n"
                        + "2025-03-01,0123-4567-8901,Sergej,150.25\n",
                StandardCharsets.UTF_8
        );

        assertThrows(IOException.class, () -> repository.findAll());
    }

    @Test
    void rejectsInvalidDateAmountAndStatus() throws IOException {
        List<String> invalidRows = List.of(
                "not-a-date,0123-4567-8901,Sergej,150.25,Pending",
                "2025-03-01,0123-4567-8901,Sergej,abc,Pending",
                "2025-03-01,0123-4567-8901,Sergej,150.25,Unknown"
        );

        for (String row : invalidRows) {
            Files.writeString(
                    csvFile,
                    HEADER + "\n" + row + "\n",
                    StandardCharsets.UTF_8
            );

            assertThrows(
                    IOException.class,
                    () -> repository.findAll(),
                    "Should reject row: " + row
            );
        }
    }

    @Test
    void doesNotOverwriteInvalidFileWhenSaveFails() throws IOException {
        String originalContent = HEADER + "\n"
                + "2025-03-01,0123-4567-8901,Sergej,abc,Pending\n";

        Files.writeString(
                csvFile,
                originalContent,
                StandardCharsets.UTF_8
        );

        var transaction = transaction(
                "Maria Johnson",
                TransactionStatus.Settled
        );

        assertThrows(
                IOException.class,
                () -> repository.save(transaction)
        );

        assertEquals(
                originalContent,
                Files.readString(csvFile, StandardCharsets.UTF_8)
        );
    }

    private static Transaction transaction(
            String holderName,
            TransactionStatus status
    ) {
        return new Transaction(
                LocalDate.of(2025, 3, 1),
                "0123-4567-8901",
                holderName,
                new BigDecimal("150.25"),
                status
        );
    }
}