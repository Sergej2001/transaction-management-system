package backend.repository;

import backend.model.Transaction;
import backend.model.TransactionStatus;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVPrinter;
import java.io.Writer;
import java.nio.file.StandardCopyOption;

@Repository
public class TransactionRepository {

    private static final List<String> HEADERS = List.of(
            "Transaction Date",
            "Account Number",
            "Account Holder Name",
            "Amount",
            "Status"
    );

    private static final CSVFormat READ_FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .get();

    private final Path filePath;

    public TransactionRepository(
            @Value("${app.transactions.file}") String filePath
    ) {
        this.filePath = Path.of(filePath);
    }

    public synchronized List<Transaction> findAll() throws IOException {
        List<Transaction> transactions = new ArrayList<>();

        try (
                Reader reader = Files.newBufferedReader(
                        filePath, StandardCharsets.UTF_8
                );
                CSVParser parser = READ_FORMAT.parse(reader)
        ) {
            if (!parser.getHeaderNames().equals(HEADERS)) {
                throw new IOException("CSV file has invalid column headers.");
            }

            for (CSVRecord record : parser) {
                transactions.add(toTransaction(record));
            }
        }

        return transactions;
    }

    private Transaction toTransaction(CSVRecord record) throws IOException {
        if (!record.isConsistent()) {
            throw new IOException(
                    "Invalid number of columns in CSV record "
                            + record.getRecordNumber()
            );
        }

        try {
            return new Transaction(
                    LocalDate.parse(record.get("Transaction Date")),
                    record.get("Account Number"),
                    record.get("Account Holder Name"),
                    new BigDecimal(record.get("Amount")),
                    TransactionStatus.valueOf(record.get("Status"))
            );
        } catch (IllegalArgumentException
                 | java.time.DateTimeException exception) {
            throw new IOException(
                    "Invalid data in CSV record " + record.getRecordNumber(),
                    exception
            );
        }
    }
    public synchronized Transaction save(Transaction transaction) throws IOException {
    List<Transaction> transactions = findAll();
    transactions.add(transaction);

    Path target = filePath.toAbsolutePath();
    Path temporaryFile = Files.createTempFile(
            target.getParent(), "transactions-", ".tmp"
    );

    try {
        try (
                Writer writer = Files.newBufferedWriter(
                        temporaryFile, StandardCharsets.UTF_8
                );
                CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)
        ) {
            printer.printRecord(HEADERS);

            for (Transaction item : transactions) {
                printer.printRecord(
                        item.transactionDate(),
                        item.accountNumber(),
                        item.accountHolderName(),
                        item.amount().toPlainString(),
                        item.status().name()
                );
            }
        }

        Files.move(
                temporaryFile,
                target,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
        );

        return transaction;
    } finally {
        Files.deleteIfExists(temporaryFile);
    }
}
}