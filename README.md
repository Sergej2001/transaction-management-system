# Transaction Management System

A web application for viewing and adding transactions, built with Java,
Spring Boot, React and Vite. Transactions are stored in a CSV file.

## Features

- Display all transactions in a table.
- Add transactions through a modal form.
- Validate input on both the frontend and backend.
- Assign a random status on the backend: Pending, Settled or Failed.
- Display status badges in yellow, green or red.
- Persist transactions across application restarts.
- Display loading states, validation messages and request errors.

## Technology stack

- Java 21
- Spring Boot 4.1.2
- Maven Wrapper
- Apache Commons CSV 1.14.1
- React 19
- Vite 8
- Jakarta Bean Validation
- JUnit and Spring MockMvc

Exact frontend dependency versions are recorded in
`frontend/package-lock.json`.

## Project structure

### Backend

Paths below are relative to `backend/`.

| Path | Responsibility |
| --- | --- |
| `src/main/java/backend/BackendApplication.java` | Starts the Spring Boot application |
| `src/main/java/backend/controller/` | GET and POST HTTP endpoints |
| `src/main/java/backend/dto/` | Request data and input validation |
| `src/main/java/backend/service/` | Transaction creation and random status assignment |
| `src/main/java/backend/repository/` | CSV reading and writing |
| `src/main/java/backend/model/` | Transaction record and status enum |
| `src/main/java/backend/exceptions/` | Validation and storage error responses |
| `src/main/resources/application.properties` | Application configuration |
| `src/test/java/backend/` | Application context, validation, repository and API tests |
| `data/transactions.csv` | Initial sample data and persisted transactions |

### Frontend

Paths below are relative to `frontend/`.

| Path | Responsibility |
| --- | --- |
| `src/main.jsx` | React entry point |
| `src/App.jsx` | Connects the transaction list, modal and page messages |
| `src/components/TransactionTable.jsx` | Displays transactions and formats amounts |
| `src/components/AddTransactionModal.jsx` | Manages form input, submission state and field errors |
| `src/hooks/useTransactions.js` | Loads and creates transactions and maintains the list |
| `src/api/transactions.js` | Sends HTTP requests and handles API error responses |
| `src/utils/transactionValidation.js` | Frontend validation rules and local date helper |
| `src/App.css` | Page, table and modal styles |
| `src/index.css` | Global styles |
| `vite.config.js` | Development server and backend proxy configuration |

## Prerequisites

Install the following software before starting:

| Software | Version | Purpose |
| --- | --- | --- |
| JDK | 21 | Compile and run the backend |
| Node.js | 26.13.0 used during development | Run the frontend tools |
| npm | 11.20.0 used during development | Install frontend dependencies |
| Git | A current Git installation | Download the repository |
| Web browser | A modern browser | Use the application |

Java distributions such as Eclipse Temurin include the JDK.
Make sure you install a JDK, not only a Java runtime.

Node.js installations normally include npm.

A separate Maven installation is not required: the repository includes
the Maven Wrapper.

Open Terminal on macOS/Linux, or PowerShell on Windows. In VS Code,
you can also open a terminal using **Terminal → New Terminal**.

Verify that the required commands are available:

```bash
java -version
javac -version
node -v
npm -v
git --version
```

Java and javac should report version 21.

If a command is not found, install the corresponding software and
open a new terminal.

An internet connection is required for the initial dependency downloads.

## Download the project

Run:

```bash
git clone https://github.com/Sergej2001/transaction-management-system.git
cd transaction-management-system
```

The directory created by this command is referred to below as the
**project root**.

## Run the application

The backend and frontend run as separate processes.
Keep two terminal windows or tabs open.

The commands below use the Maven Wrapper syntax for macOS/Linux.
On Windows PowerShell, replace `./mvnw` with `.\mvnw.cmd`.

### 1. Start the backend

In the first terminal, starting from the project root:

```bash
cd backend
./mvnw spring-boot:run
```

The first run may take longer while Maven downloads dependencies.

Wait until the terminal reports that the application has started.

The backend is available at:

```text
http://localhost:8080
```

To check the API, open this URL in your browser:

```text
http://localhost:8080/transactions
```

You should see a JSON array of transactions.

Keep this terminal running.

### 2. Start the frontend

Open a second terminal and navigate to the project root.
Then run:

```bash
cd frontend
npm ci
npm run dev
```

`npm ci` installs the dependencies recorded in the lockfile.
Run it after cloning the repository or when the lockfile changes.

Open:

```text
http://localhost:5173
```

The transaction table should load from the backend.

The frontend development server forwards `/transactions` requests
to `http://localhost:8080` through the Vite proxy.

Keep both terminals running while using the application.

### 3. Stop the application

Press **Ctrl + C** in each terminal to stop its process.

## Configuration

No environment variables are required for the default setup.

The backend configuration is located at:

```text
backend/src/main/resources/application.properties
```

The CSV location is configured with:

```properties
app.transactions.file=${TRANSACTIONS_FILE:data/transactions.csv}
```

The default path is relative to the backend process's working directory.
Start the backend from the `backend` folder as shown above.

### Optional custom CSV location

On macOS/Linux, run the following from the `backend` folder,
replacing the example path with your file's absolute path:

```bash
TRANSACTIONS_FILE="/absolute/path/transactions.csv" ./mvnw spring-boot:run
```

On Windows PowerShell:

```powershell
$env:TRANSACTIONS_FILE = "C:\path\transactions.csv"
.\mvnw.cmd spring-boot:run
```

The custom file must already exist and have the expected CSV headers.
The application must have permission to read the file and write
to its containing directory.

No `.env` file is required.

## CSV storage

The default file is:

```text
backend/data/transactions.csv
```

The expected column order is:

```csv
Transaction Date,Account Number,Account Holder Name,Amount,Status
```

Example:

```csv
Transaction Date,Account Number,Account Holder Name,Amount,Status
2025-03-01,7289-3445-1121,Maria Johnson,150.00,Settled
```

Files are read and written using UTF-8.

Apache Commons CSV handles quoting and escaping, including commas
and quotes inside fields.

When adding a transaction, the application reads the existing records,
writes the updated contents to a temporary file in the same directory,
and atomically replaces the original file.

Repository methods are synchronized to prevent overlapping operations
through the repository instance used by this application.

## Validation and assumptions

The following validation rules are application decisions.
The assignment's sample data guided the account-number format.

| Field | Rule |
| --- | --- |
| Transaction date | Required; a valid date in `YYYY-MM-DD` format; not in the future |
| Account number | Required; format `1234-5678-9012`; leading zeros are preserved |
| Account holder name | Required; at least one Unicode letter; maximum 100 characters |
| Amount | Required; greater than zero; up to 9 integer digits and 2 decimal places |

Names may contain Unicode letters, combining marks, spaces, periods,
commas, apostrophes and hyphens. Digits are not accepted.
This is a simplified rule for individual account holders, not a
universal validation rule for all personal or business names.

Surrounding whitespace is removed from account numbers and names.

The maximum amount is `999999999.99`.
Amounts are represented using `BigDecimal` on the backend.
No currency is assumed because the assignment does not specify one.

The browser uses its local date for frontend validation.
The server's local date is authoritative for backend validation.

Backend validation applies independently of the frontend.
Invalid input is rejected before CSV storage is called.

These input checks apply to new transactions; they do not rewrite
existing CSV records.

## Status assignment

The backend randomly selects one of:

- `Pending`
- `Settled`
- `Failed`

The selected status is stored in the CSV and returned in the response.
Reading transactions does not assign new statuses.

Statuses are simulated; no payment processing takes place.
A transaction assigned `Failed` is still successfully stored and
returns HTTP `201 Created`.

## API documentation

Base URL:

```text
http://localhost:8080
```

No authentication is required.

### GET /transactions

Returns all stored transactions.

Successful response: **200 OK**

Example response:

```json
[
  {
    "transactionDate": "2025-03-01",
    "accountNumber": "7289-3445-1121",
    "accountHolderName": "Maria Johnson",
    "amount": 150.00,
    "status": "Settled"
  }
]
```

A CSV containing only the header returns an empty array: `[]`.

Example request:

```bash
curl -i http://localhost:8080/transactions
```

### POST /transactions

Adds a transaction and assigns its status on the backend.
The request does not need a status field.

Request body:

```json
{
  "transactionDate": "2025-03-01",
  "accountNumber": "0123-4567-8901",
  "accountHolderName": "Sergej Pajović",
  "amount": 150.25
}
```

Successful response: **201 Created**

Example response:

```json
{
  "transactionDate": "2025-03-01",
  "accountNumber": "0123-4567-8901",
  "accountHolderName": "Sergej Pajović",
  "amount": 150.25,
  "status": "Pending"
}
```

The actual status is randomly selected.

Example request for macOS/Linux:

```bash
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "transactionDate": "2025-03-01",
    "accountNumber": "0123-4567-8901",
    "accountHolderName": "Sergej Pajović",
    "amount": 150.25
  }'
```

Example request for Windows PowerShell:

```powershell
$body = @{
    transactionDate = "2025-03-01"
    accountNumber = "0123-4567-8901"
    accountHolderName = "Sergej Pajovic"
    amount = 150.25
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8080/transactions" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

These POST examples add a real row to the configured CSV.

### Error responses

Validation failure: **400 Bad Request**

Example:

```json
{
  "message": "Please check the entered data.",
  "fieldErrors": {
    "amount": "Amount must be greater than zero"
  }
}
```

Malformed JSON or an unreadable field value also returns
**400 Bad Request** with a message.

CSV access or parsing failure: **500 Internal Server Error**

```json
{
  "message": "Unable to access transaction data. Please try again later.",
  "fieldErrors": {}
}
```

Storage errors are logged on the backend.
Internal file paths and stack traces are not included in this response.

## Testing

### Backend automated tests

Starting from the project root:

```bash
cd backend
./mvnw test
```

On Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd test
```

The backend does not need to be running for these tests.

The tests cover:

- Application context startup.
- Valid and invalid transaction input.
- CSV reading and writing.
- Persistence through a new repository instance.
- Preservation of existing records.
- Unicode, commas and quotes in stored values.
- CSV input without a final newline.
- Missing files, invalid headers and invalid records.
- GET and POST responses.
- Rejection of invalid requests without changing the CSV.
- Storage error responses.

Repository and controller tests use temporary CSV files.
They do not modify `backend/data/transactions.csv`.

Controller tests use MockMvc with the real service and repository,
plus an explicitly configured validator and exception handler.
They do not start a network server.

### Frontend checks

Starting from the project root:

```bash
cd frontend
npm ci
npm run lint
npm run build
```

Lint checks source-code issues.
The build command creates frontend production assets in `frontend/dist`.

These checks do not replace manual browser testing.

The documented way to run the complete application locally is
`npm run dev` together with the backend.
A separate production hosting configuration is outside this project's scope.

### Manual checks

With both applications running:

1. Confirm that transactions load into the table.
2. Open the Add Transaction modal.
3. Submit valid data and confirm that the new row appears.
4. Check that its status is Pending, Settled or Failed with the correct color.
5. Refresh the page and confirm that the transaction remains.
6. Restart the backend and confirm that the transaction remains.
7. Try a future date, an invalid account, a name containing digits,
   and a negative amount. Confirm that errors appear and no row is added.
8. Try zero, more than two decimal places and an amount above the limit.
9. Confirm that names containing accented letters and apostrophes work.
10. Stop the backend and reload the page. Confirm that a loading error appears.
11. Restart the backend and use Retry to load the table again.
12. Check the modal and table at a narrow browser width and using the keyboard.

Manual additions remain in the CSV. Use a separate CSV copy through
`TRANSACTIONS_FILE` if you want to preserve the supplied sample file.

## Architecture

The backend separates responsibilities into:

- **Controller:** HTTP endpoints and request validation.
- **DTO:** Incoming transaction data and validation constraints.
- **Service:** Transaction creation and random status assignment.
- **Repository:** CSV reading and persistence.
- **Exception handler:** Consistent validation and storage error responses.
- **Model:** Transaction data and available statuses.

The frontend separates presentation, state management and HTTP requests:

- `App` connects the page components and controls modal visibility.
- `TransactionTable` displays the supplied transactions.
- `AddTransactionModal` manages form values, submission state and errors.
- `useTransactions` owns the transaction list, loading state and load errors.
  It also creates transactions through the API and appends successful
  responses to the list.
- The API module handles HTTP requests.
- The validation module checks form values before submission.

The hook is called once in `App`. Its creation function is passed to the
modal, so both components work with the same transaction list.

Backend validation remains authoritative.

## Limitations

- CSV storage is intended for a small dataset and one backend instance.
- Each write reads and rewrites the entire dataset.
- Synchronization does not coordinate multiple application processes
  or external programs editing the CSV.
- Atomic replacement requires filesystem support; there is no
  non-atomic fallback.
- There is no authentication, pagination, editing or deletion.
- Repeated successful POST requests create separate rows; there is no
  server-side duplicate detection or idempotency mechanism.
- Frontend automated component and end-to-end tests are not included.

## Troubleshooting

### The frontend cannot load transactions

Confirm that the backend is running on port `8080`.

Open `http://localhost:8080/transactions` directly to check it.
Also check the backend terminal for errors.

If you change `frontend/vite.config.js`, restart the frontend server.

### The backend cannot find the CSV

Run the backend from the `backend` directory.
Check that `data/transactions.csv` exists, or verify your
`TRANSACTIONS_FILE` setting.

### A port is already in use

Stop the previous application process using **Ctrl + C** in its terminal.

The frontend is configured to use port `5173` without automatically
switching to a different port.

### Permission denied when running the Maven Wrapper

On macOS/Linux, from the `backend` directory:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

### Dependency downloads fail

Check your internet connection and access to the Maven and npm registries.
Then retry the command.
