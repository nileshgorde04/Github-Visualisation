# Git Contributions Visualization Tool

A tool to visualize Git contributions across multiple repositories, built with Spring Boot and React.

![Git Contributions Visualization](gitcontribs.png)

## Features

- Find Git repositories within a directory structure
- Analyze repositories for commits in a specified time period
- Filter commits by user
- Visualize contributions in a GitHub-style contribution graph
- View statistics about contributions

## Technologies Used

- **Backend**: Spring Boot, JGit
- **Frontend**: React, Bootstrap
- **Build Tools**: Maven, npm

## Project Structure

- `src/main/java`: Spring Boot backend code
- `frontend`: React frontend code
- `gitcontribs.py`: Original Python implementation

## Getting Started

### Prerequisites

- Java 11 or higher
- Node.js and npm
- Git

### Running the Backend

1. Clone the repository
2. Navigate to the project root directory
3. Build the project with Maven:
   ```
   mvn clean install
   ```
4. Run the Spring Boot application:
   ```
   mvn spring-boot:run
   ```
   
The backend will start on http://localhost:8080

### Running the Frontend

1. Navigate to the frontend directory:
   ```
   cd frontend
   ```
2. Install dependencies:
   ```
   npm install
   ```
3. Start the development server:
   ```
   npm start
   ```

The frontend will start on http://localhost:3000

## Usage

1. Enter the root directory where you want to search for Git repositories
2. Specify the number of days to analyze
3. Optionally, enter your Git user email (if not provided, it will use the email from your Git configuration)
4. Click "Analyze Contributions"
5. View the contribution graph and statistics

## Original Python Implementation

The project also includes a Python implementation (`gitcontribs.py`) that can be run from the command line:

```
python gitcontribs.py --root /path/to/search --days 30 --email your.email@example.com
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.