# Employee Management System

A full-stack web application for managing employee biodata, authentication, and CSV import/export operations.

## Features

- **User Authentication** — Session-based login with role-based access control (Admin/User)
- **CRUD Operations** — Create, read, update, delete employee records
- **Search & Pagination** — Search by name/city with paginated results
- **CSV Import** — Upload employees via CSV with validation and error reporting
- **CSV/PDF Export** — Download records as CSV or PDF
- **Email Notifications** — Automated emails for password resets and error reports
- **Dynamic Forms** — Cascading state/city dropdowns with client-side validation

## Tech Stack

### Backend
- Spring Boot 4.0.3 (Java 17)
- Spring Security (BCrypt password hashing)
- Spring Data JPA + Hibernate
- MySQL Database
- iText (PDF generation)
- JavaMail (SMTP)

### Frontend
- React 19.2.4
- React Router v7
- Axios (HTTP client)
- Bootstrap 5
- React Toastify (notifications)

## Prerequisites

- **Java 17+** (backend)
- **Node.js 16+** (frontend)
- **MySQL 8+**
- **Git**

## Setup Instructions

### Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd demo
   ```

2. Copy environment variables:
   ```bash
   copy .env.example .env
   ```

3. Edit `.env` with your database and mail credentials

4. Build and run the Spring Boot app:
   ```bash
   mvnw.cmd clean install
   mvnw.cmd spring-boot:run
   ```

   Backend will start on `http://localhost:8080`

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd ReactApp
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the React development server:
   ```bash
   npm start
   ```

   Frontend will open at `http://localhost:3000`

## API Endpoints

### Authentication
- `POST /api/login` — Login with email and password
- `GET /api/check-session` — Validate current session
- `POST /api/logout` — Logout user

### Employee Management
- `GET /api/biodata` — Get paginated employee list
- `GET /api/biodata/{id}` — Get employee by ID
- `POST /api/submit` — Create new employee
- `PUT /api/biodata/{id}` — Update employee
- `DELETE /api/biodata/{id}` — Delete employee
- `GET /api/biodata/search?keyword=...` — Search employees

### File Operations
- `POST /api/upload/csv` — Upload CSV file
- `GET /api/download/csv` — Download all as CSV
- `POST /api/download/csv/selected` — Download selected as CSV
- `GET /api/download/pdf` — Download all as PDF
- `POST /api/download/pdf/selected` — Download selected as PDF

## Default Credentials

**Admin Account:**
- Email: `admin@gmail.com`
- Password: `admin123`

## Database Schema

### Tables
- `users` — Employee records with fields: name, email, phone, dob, age, gender, address, city, state, country, password, role
- `user_skills` — Skills collection for each user

## Configuration

### application.properties

Edit `demo/src/main/resources/application.properties` to configure:
- Database connection
- Mail SMTP settings
- Session timeout (default: 2 hours)
- Logging levels

## Project Structure

```
sprint1-final/
├── demo/                          # Spring Boot backend
│   ├── src/main/java/
│   │   └── com/example/demo/
│   │       ├── config/            # Security & CORS config
│   │       ├── controller/        # REST endpoints
│   │       ├── service/           # Business logic
│   │       ├── repository/        # Database access
│   │       ├── exceptionHandler/  # Error handling
│   │       └── scheduler/         # Scheduled tasks
│   ├── src/main/resources/
│   │   └── application.properties # Configuration
│   └── pom.xml                    # Maven dependencies
│
└── ReactApp/                      # React frontend
    ├── src/
    │   ├── App.js                 # Main app router
    │   ├── Login.js               # Login page
    │   ├── Biodata.js             # Create/Edit form
    │   ├── BiodataTable.js        # List & manage records
    │   ├── ViewBiodata.js         # User profile view
    │   ├── ProtectedRouter.js     # Route guard
    │   └── index.js               # Entry point
    ├── public/                    # Static assets
    └── package.json               # NPM dependencies
```

## Key Implementation Details

### Backend
- **Authentication:** Session-based with 2-hour timeout
- **Password Security:** BCrypt hashing with email notifications
- **CSV Processing:** Row-level validation, duplicate detection, error reporting via email
- **Error Handling:** Centralized exception handler with consistent responses
- **Validation:** Jakarta validation annotations for all input fields

### Frontend
- **Protected Routes:** Role-based access control
- **Form Handling:** Client-side validation with real-time error feedback
- **Search:** Debounced search (500ms) for better performance
- **Pagination:** 4 records per page with navigation controls
- **File Operations:** CSV upload with progress, CSV/PDF download support
- **Bulk Operations:** Select/deselect all records, bulk delete, bulk export

## Future Improvements

- [ ] JWT authentication (replace session-based auth)
- [ ] Async CSV processing with background job queues
- [ ] Rate limiting on login endpoint
- [ ] Unit and integration tests
- [ ] Docker containerization
- [ ] Deployment to cloud (AWS/Heroku)
- [ ] Two-factor authentication (2FA)
- [ ] Audit logging for record changes

## Environment Variables

Create a `.env` file in the root directory (use `.env.example` as template):

```
DB_USERNAME=root
DB_PASSWORD=your_password
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
SERVER_PORT=8080
LOG_LEVEL=INFO
```

**Important:** Never commit `.env` file! Use `.env.example` as a template.

## Contributing

Contributions are welcome! Feel free to fork and submit pull requests.

## License

MIT License — See LICENSE file for details

## Author

Naveen V

## Contact

- Email: naveenvajjiravel@gmail.com
- GitHub: [Your GitHub Profile]

---

**Note:** Never commit real credentials. Always use environment variables!
