export async function getTransactions() {
  const response = await fetch('/transactions');

  if (!response.ok) {
    throw new Error('Unable to load transactions. Please try again.');
  }

  return response.json();
}

export async function createTransaction(transaction) {
  const response = await fetch('/transactions', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(transaction),
  });

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null);
    const error = new Error(
      errorBody?.message || 'Unable to save the transaction.'
    );

    error.fieldErrors = errorBody?.fieldErrors || {};
    throw error;
  }

  return response.json();
}