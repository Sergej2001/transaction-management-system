import { useState } from 'react';
import AddTransactionModal from './components/AddTransactionModal';
import TransactionTable from './components/TransactionTable';
import useTransactions from './hooks/useTransactions';
import './App.css';

export default function App() {
  const {
    transactions,
    loading,
    error,
    reload,
    addTransaction,
  } = useTransactions();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [successMessage, setSuccessMessage] = useState('');

  function handleOpenModal() {
    setSuccessMessage('');
    setIsModalOpen(true);
  }

  function handleTransactionCreated() {
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
          onClick={handleOpenModal}
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
              onClick={reload}
            >
              Try again
            </button>
          </div>
        ) : transactions.length === 0 ? (
          <p className="message">No transactions yet.</p>
        ) : (
          <TransactionTable transactions={transactions} />
        )}
      </section>

      {isModalOpen && (
        <AddTransactionModal
          onClose={() => setIsModalOpen(false)}
          onSubmit={addTransaction}
          onCreated={handleTransactionCreated}
        />
      )}
    </main>
  );
}