import { useEffect, useState } from 'react';
import { getTransactions } from './api/transactions';
import AddTransactionModal from './components/AddTransactionModal';
import './App.css';

const amountFormatter = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export default function App() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadCount, setReloadCount] = useState(0);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  useEffect(() => {
    let ignore = false;

    async function loadTransactions() {
      setLoading(true);
      setError('');

      try {
        const data = await getTransactions();

        if (!ignore) {
          setTransactions(data);
        }
      } catch {
        if (!ignore) {
          setError(
            'Unable to load transactions. Check that the backend is running and try again.'
          );
        }
      } finally {
        if (!ignore) {
          setLoading(false);
        }
      }
    }

    loadTransactions();

    return () => {
      ignore = true;
    };
  }, [reloadCount]);

  function handleTransactionCreated(transaction) {
    setTransactions((previous) => [...previous, transaction]);
    setIsModalOpen(false);
    setSuccessMessage('Transaction added successfully.');
  }

  return (
    <main className="page">
      <header className="page-header">
        <div>
          <p className="eyebrow">TRANSACTION MANAGEMENT</p>
          <h1>Transactions</h1>
          <p className="subtitle">
            View account transactions and their current status.
          </p>
        </div>

        <button
          type="button"
          className="primary-button"
          disabled={loading || Boolean(error)}
          onClick={() => {
            setSuccessMessage('');
            setIsModalOpen(true);
          }}
        >
          + Add Transaction
        </button>
      </header>

      {successMessage && (
        <p className="success-message" role="status">
          {successMessage}
        </p>
      )}

      <section className="panel" aria-label="Transaction list">
        <div className="panel-header">
          <h2>All transactions</h2>

          {!loading && !error && (
            <span className="count">{transactions.length} total</span>
          )}
        </div>

        {loading ? (
          <p className="message" role="status">
            Loading transactions…
          </p>
        ) : error ? (
          <div className="message">
            <p className="error" role="alert">
              {error}
            </p>

            <button
              type="button"
              className="retry-button"
              onClick={() => setReloadCount((count) => count + 1)}
            >
              Try again
            </button>
          </div>
        ) : transactions.length === 0 ? (
          <p className="message">No transactions yet.</p>
        ) : (
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th scope="col">Transaction Date</th>
                  <th scope="col">Account Number</th>
                  <th scope="col">Account Holder Name</th>
                  <th scope="col" className="amount">
                    Amount
                  </th>
                  <th scope="col">Status</th>
                </tr>
              </thead>

              <tbody>
                {transactions.map((transaction, index) => (
                  <tr key={index}>
                    <td>{transaction.transactionDate}</td>
                    <td className="account-number">
                      {transaction.accountNumber}
                    </td>
                    <td>{transaction.accountHolderName}</td>
                    <td className="amount">
                      {amountFormatter.format(transaction.amount)}
                    </td>
                    <td>
                      <span
                        className={`status status-${transaction.status.toLowerCase()}`}
                      >
                        {transaction.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {isModalOpen && (
        <AddTransactionModal
          onClose={() => setIsModalOpen(false)}
          onCreated={handleTransactionCreated}
        />
      )}
    </main>
  );
}