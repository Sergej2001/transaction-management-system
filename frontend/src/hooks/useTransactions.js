import { useEffect, useState } from 'react';
import {
  getTransactions,
  createTransaction,
} from '../api/transactions';

export default function useTransactions() {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadCount, setReloadCount] = useState(0);

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

  function reload() {
    setReloadCount((count) => count + 1);
  }

  async function addTransaction(values) {
    const transaction = await createTransaction(values);

    setTransactions((previous) => [...previous, transaction]);

    return transaction;
  }

  return {
    transactions,
    loading,
    error,
    reload,
    addTransaction,
  };
}