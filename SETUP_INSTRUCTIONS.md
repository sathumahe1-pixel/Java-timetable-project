# Timetable Generator - Setup Instructions

This project allows users to login via a web browser and automatically launch the Java Timetable Generator application.

## Requirements
- Java JDK 8 or higher installed
- `javac` and `java` commands available in your PATH
- Windows OS (as per current implementation)

## How to Use

### Starting the Application

1. **Double-click `start-server.bat`** in the project root directory
   - This will compile the LoginServer and start the HTTP server
   - Your browser will automatically open to `http://localhost:8080/`

2. **Login**
   - Username: `Admin`
   - Password: `12345`

3. **After successful login:**
   - The Timetable Generator Java Swing application will launch automatically
   - The login page will stay open (as requested)
   - You can see the success message on the login page

### Manual Start (Alternative)

If you prefer to start manually:

```batch
# Compile the server
javac LoginServer.java

# Run the server
java LoginServer

# Open browser to http://localhost:8080/login.html
```

## Project Structure

```
Java-timetable-project/
├── LoginServer.java                      # HTTP server for login
├── start-server.bat                      # Windows batch script to start server
├── login.html                            # Login page
├── dashboard.html                        # Dashboard page (legacy)
├── BG.jpg                                # Background image
└── Automatic-TimeTable-Generator/
    └── src/
        └── TimetableGeneratorApp.java    # Main timetable application
```

## How It Works

1. **LoginServer.java** creates a simple HTTP server on port 8080
2. It serves the static HTML files (login.html, BG.jpg, etc.)
3. When you submit login credentials, it validates them server-side
4. Upon successful authentication, it:
   - Compiles `TimetableGeneratorApp.java` (if not already compiled)
   - Launches the Java Swing application
   - Returns a success message to the browser

## Troubleshooting

### Server won't start
- Make sure Java JDK is installed: `java -version` and `javac -version`
- Check if port 8080 is already in use
- Run `start-server.bat` from the project root directory

### Login fails
- Verify credentials: Username=`Admin`, Password=`12345`
- Check browser console for errors (F12)

### Timetable app doesn't launch
- Make sure Java is properly installed
- Check the server console for error messages
- Verify that `Automatic-TimeTable-Generator/src/TimetableGeneratorApp.java` exists

## Notes

- The server runs on `localhost:8080` only (not accessible from other machines)
- This is designed for local demo purposes
- The login page stays open after successful login
- You can login multiple times, but only one instance of the Timetable Generator will run
