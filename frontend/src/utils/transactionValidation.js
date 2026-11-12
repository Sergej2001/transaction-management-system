export function getToday() {
  const today = new Date();
  const year = today.getFullYear();
  const month = String(today.getMonth() + 1).padStart(2, '0');
  const day = String(today.getDate()).padStart(2, '0');

  return `${year}-${month}-${day}`;
}

export function validateForm(form) {
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
      'Enter a valid name without numbers or unsupported symbols.';
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