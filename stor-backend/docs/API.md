# Stor API Documentation

Base URL: `http://localhost:8080/api/v1` (local) or `https://stor-backend.onrender.com/api/v1` (prod)

## Authentication
All protected routes require an `Authorization` header with a Bearer token.
`Authorization: Bearer <access_token>`

### POST `/auth/register`
Creates a new user.
**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "securepassword123"
}
```
**Response (201 Created):**
```json
{
  "access_token": "ey...",
  "refresh_token": "uuid-string",
  "user": {
    "id": "uuid-string",
    "name": "John Doe",
    "email": "john@example.com"
  }
}
```

### POST `/auth/login`
**Request Body:**
```json
{
  "email": "john@example.com",
  "password": "securepassword123"
}
```
**Response (200 OK):** Same as register.

### POST `/auth/refresh`
**Request Body:**
```json
{
  "refresh_token": "uuid-string"
}
```
**Response (200 OK):**
```json
{
  "access_token": "ey...",
  "refresh_token": "new-uuid-string"
}
```

## Expenses
### GET `/expenses`
Query Params: `?category=Food&month=5&year=2024&search=lunch&page=1&pageSize=50`
### POST `/expenses`
**Request Body:**
```json
{
  "title": "Lunch",
  "amount": 450.0,
  "category": "Food",
  "payment_method": "Cash",
  "date": "2024-05-21",
  "notes": "Optional"
}
```

## Income
### GET `/income`
Query Params: `?month=5&year=2024`
### POST `/income`
**Request Body:**
```json
{
  "source": "Salary",
  "amount": 60000.0,
  "date": "2024-05-20"
}
```

## Loans
### GET `/loans`
Query Params: `?status=active` (active or archived)
### POST `/loans`
**Request Body:**
```json
{
  "name": "Car Loan",
  "lender": "Bank",
  "original_amount": 150000.0,
  "interest_rate": 12.0,
  "monthly_payment": 12500.0,
  "due_day": 5,
  "start_date": "2024-01-05"
}
```

## Repayments
### GET `/loans/{loanId}/repayments`
### POST `/loans/{loanId}/repayments`
**Request Body:**
```json
{
  "amount_paid": 12500.0,
  "date": "2024-05-05"
}
```

## Dashboard
### GET `/dashboard`
Aggregated data for the main screen.

## Reports
### GET `/reports/monthly`
Query Params: `?month=5&year=2024`
### GET `/reports/yearly`
Query Params: `?year=2024`
### GET `/reports/categories`
Query Params: `?month=5&year=2024`

## Search
### GET `/search`
Query Params: `?q=lunch&type=expense`
Type can be `expense`, `income`, `loan`, `repayment`, or left out for global search.

