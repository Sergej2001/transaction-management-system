package backend.service;

import backend.dto.CreateTransactionRequest;
import backend.model.Transaction;
import backend.model.TransactionStatus;
import backend.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(
            TransactionRepository transactionRepository
    ) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> findAll() throws IOException {
        return transactionRepository.findAll();
    }

    public Transaction create(CreateTransactionRequest request)
            throws IOException {

        TransactionStatus[] statuses = TransactionStatus.values();
        int index = ThreadLocalRandom.current().nextInt(statuses.length);

        Transaction transaction = new Transaction(
                request.transactionDate(),
                request.accountNumber().strip(),
                request.accountHolderName().strip(),
                request.amount(),
                statuses[index]
        );

        return transactionRepository.save(transaction);
    }
}