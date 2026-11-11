import { useEffect, useRef, useState } from 'react';
import { createTransaction } from '../api/transactions';

const initialForm = {
  transactionDate: '',
  accountNumber: '',
  accountHolderName: '',
  amount: '',
};

function getToday() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

function validateForm(form) {
  const errors = {};
  const date = form.transactionDate;
  const parsedDate = new Date(`${date}T00:00:00Z`);

  if (
    !/^\d{4}-\d{2}-\d{2}$/.test(date) ||
    !Number.isFinite(parsedDate.getTime()) ||
    parsedDate.toISOString().slice(0, 10) !== date ||
    date.startsWith('0000')
  ) {
    errors.transactionDate = 'Enter a valid transaction date.';
  } else if (date > getToday()) {
    errors.transactionDate = 'Transaction date cannot be in the future.';
  }

  if (!/^[0-9]{4}-[0-9]{4}-[0-9]{4}$/.test(form.accountNumber.trim())) {
    errors.accountNumber = 'Use the format 1234-5678-9012.';
  }

  const name = form.accountHolderName.trim();

  if (!name) {
    errors.accountHolderName = 'Account holder name is required.';
  } else if (name.length > 100) {
    errors.accountHolderName = 'Name must contain at most 100 characters.';
  } else if (
    !/\p{L}/u.test(name) ||
    !/^[\p{L}\p{M} .,'’-]+$/u.test(name)
  ) {
    errors.accountHolderName =
      'Enter a valid name. Numbers and special symbols are not allowed.';
  }

  const amount = form.amount.trim();

  if (!amount) {
    errors.amount = 'Amount is required.';
  } else if (!Number.isFinite(Number(amount)) || Number(amount) <= 0) {
    errors.amount = 'Amount must be greater than zero.';
  } else if (!/^\d{1,9}(\.\d{1,2})?$/.test(amount)) {
    errors.amount =
      'Use at most 9 whole-number digits and 2 decimal places.';
  }

  return errors;
}

export default function AddTransactionModal({ onClose, onCreated }) {
  const dialogRef = useRef(null);
  const submittingRef = useRef(false);

  const [form, setForm] = useState(initialForm);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  useEffect(() => {
    const dialog = dialogRef.current;
    dialog.showModal();

    return () => {
      dialog.close();
    };
  }, []);

  function handleChange(event) {
    const { name, value } = event.target;

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }));

    setFieldErrors((previous) => ({
      ...previous,
      [name]: '',
    }));

    setError('');
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (submittingRef.current) {
      return;
    }

    const errors = validateForm(form);
    setFieldErrors(errors);
    setError('');

    const firstInvalidField = Object.keys(errors)[0];

    if (firstInvalidField) {
      dialogRef.current
        .querySelector(`[name="${firstInvalidField}"]`)
        ?.focus();
      return;
    }

    submittingRef.current = true;
    setSaving(true);

    try {
      const transaction = await createTransaction({
        transactionDate: form.transactionDate,
        accountNumber: form.accountNumber.trim(),
        accountHolderName: form.accountHolderName.trim(),
        amount: Number(form.amount),
      });

      onCreated(transaction);
    } catch (requestError) {
      setError(
        requestError instanceof TypeError
          ? 'Unable to reach the server. Check your connection.'
          : requestError.message
      );

      setFieldErrors(requestError.fieldErrors || {});
    } finally {
      submittingRef.current = false;
      setSaving(false);
    }
  }

  function handleClose() {
    if (!submittingRef.current) {
      onClose();
    }
  }

  function handleCancel(event) {
    event.preventDefault();
    handleClose();
  }

  return (
    <dialog
      ref={dialogRef}
      className="transaction-modal"
      aria-labelledby="modal-title"
      onCancel={handleCancel}
    >
      <div className="modal-header">
        <div>
          <h2 id="modal-title">Add transaction</h2>
          <p>Enter the transaction details below.</p>
        </div>

        <button
          type="button"
          className="close-button"
          aria-label="Close dialog"
          disabled={saving}
          onClick={handleClose}
        >
          ×
        </button>
      </div>

      <form onSubmit={handleSubmit} aria-busy={saving} noValidate>
        <fieldset className="form-fields" disabled={saving}>
          <div className="form-field">
            <label htmlFor="transactionDate">Transaction Date</label>
            <input
              id="transactionDate"
              name="transactionDate"
              type="date"
              max={getToday()}
              value={form.transactionDate}
              onChange={handleChange}
              required
              autoFocus
              aria-invalid={Boolean(fieldErrors.transactionDate)}
              aria-describedby={
                fieldErrors.transactionDate ? 'date-error' : undefined
              }
            />
            {fieldErrors.transactionDate && (
              <span id="date-error" className="field-error" role="alert">
                {fieldErrors.transactionDate}
              </span>
            )}
          </div>

          <div className="form-field">
            <label htmlFor="accountNumber">Account Number</label>
            <input
              id="accountNumber"
              name="accountNumber"
              type="text"
              placeholder="1234-5678-9012"
              value={form.accountNumber}
              onChange={handleChange}
              required
              aria-invalid={Boolean(fieldErrors.accountNumber)}
              aria-describedby={
                fieldErrors.accountNumber ? 'account-error' : undefined
              }
            />
            {fieldErrors.accountNumber && (
              <span id="account-error" className="field-error" role="alert">
                {fieldErrors.accountNumber}
              </span>
            )}
          </div>

          <div className="form-field">
            <label htmlFor="accountHolderName">Account Holder Name</label>
            <input
              id="accountHolderName"
              name="accountHolderName"
              type="text"
              placeholder="Full name"
              value={form.accountHolderName}
              onChange={handleChange}
              required
              aria-invalid={Boolean(fieldErrors.accountHolderName)}
              aria-describedby={
                fieldErrors.accountHolderName ? 'name-error' : undefined
              }
            />
            {fieldErrors.accountHolderName && (
              <span id="name-error" className="field-error" role="alert">
                {fieldErrors.accountHolderName}
              </span>
            )}
          </div>

          <div className="form-field">
            <label htmlFor="amount">Amount</label>
            <input
              id="amount"
              name="amount"
              type="number"
              min="0.01"
              max="999999999.99"
              step="0.01"
              placeholder="0.00"
              value={form.amount}
              onChange={handleChange}
              required
              aria-invalid={Boolean(fieldErrors.amount)}
              aria-describedby={
                fieldErrors.amount ? 'amount-error' : undefined
              }
            />
            {fieldErrors.amount && (
              <span id="amount-error" className="field-error" role="alert">
                {fieldErrors.amount}
              </span>
            )}
          </div>
        </fieldset>

        <p className="form-hint">
          A status is assigned automatically when the transaction is saved.
        </p>

        {error && (
          <p className="form-error" role="alert">
            {error}
          </p>
        )}

        <div className="modal-actions">
          <button
            type="button"
            className="secondary-button"
            disabled={saving}
            onClick={handleClose}
          >
            Cancel
          </button>

          <button
            type="submit"
            className="primary-button"
            disabled={saving}
          >
            {saving ? 'Saving…' : 'Save transaction'}
          </button>
        </div>
      </form>
    </dialog>
  );
}