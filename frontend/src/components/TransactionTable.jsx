const amountFormatter = new Intl.NumberFormat('en-US', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export default function TransactionTable({ transactions }) {
  return (
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
  );
}