# B202 Student Management System

## Project Overview

The Student Management System is a console-based Java application developed for the B202 Advanced Programming module at Gisma University of Applied Sciences.

The application demonstrates Object-Oriented Programming (OOP) principles through student record management, including registration, searching, updating, deletion, statistics and persistent local storage.

## Technologies Used

- Programming language: Java 21
- Development environment: Visual Studio Code
- Java Development Kit: Eclipse Temurin JDK 21
- Data storage: Local text file (`students.txt`)
- Testing: Built-in Java automated tests
- Version control: GitHub

No external dataset, database or third-party libraries are required.

## Application Features

1. **Add Student:** Register undergraduate and postgraduate students.
2. **View Students:** Display all registered student records.
3. **Search Student:** Find a student using their unique ID.
4. **Update Student:** Modify an existing student's information.
5. **Delete Student:** Remove a student record.
6. **Student Statistics:** Display total, undergraduate and postgraduate student counts.
7. **Exit:** Close the application.

The system also provides input validation, duplicate-ID prevention and automatic saving of changes.

## Object-Oriented Programming Implementation

The application demonstrates four fundamental OOP principles:

- **Encapsulation:** Student attributes are private and accessed through methods.
- **Abstraction:** The abstract `Student` class defines shared attributes and behaviour.
- **Inheritance:** `UndergraduateStudent` and `PostgraduateStudent` extend the `Student` class.
- **Polymorphism:** Each student subclass overrides the `level()` method to return its student category.

The source file contains five classes:

| Class | Responsibility |
|---|---|
| `StudentManagementSystem` | Main application and console menu |
| `Student` | Abstract base class |
| `UndergraduateStudent` | Undergraduate student implementation |
| `PostgraduateStudent` | Postgraduate student implementation |
| `StudentManager` | Record management and file operations |

All classes are contained in `StudentManagementSystem.java`.

## Installation Requirements

Before running the application, install Java JDK 21 or a compatible newer JDK.

Verify installation using:

```bash
java -version
javac -version
```

## How to Run the Application

**Step 1:** Download or clone the repository and open its folder in VS Code or a terminal.

**Step 2:** Compile the Java source file:

```bash
javac StudentManagementSystem.java
```

**Step 3:** Run the application:

```bash
java StudentManagementSystem
```

**Step 4:** Select a menu option and follow the instructions displayed in the terminal.

## Automated Testing

The application includes 11 built-in automated checks covering student management, duplicate prevention, searching, polymorphism, file persistence and deletion.

Run the tests using:

```bash
java StudentManagementSystem --self-test
```

The completed test run produced:

```text
ALL 11 TESTS PASSED.
```

Manual testing also demonstrated successful registration, viewing, searching, updating, deletion and statistics.

## Data Storage

The system uses Java collections to manage student objects in memory.

Student records are saved to `students.txt` in the application's working directory. The file is created when changes are saved and can be loaded when the application restarts.

## Project Limitations

The application currently uses a console interface and local file storage. It does not include user authentication, a graphical interface or a database.

## Future Improvements

Potential enhancements include:

- JavaFX graphical user interface
- Database integration
- User authentication
- Advanced searching and filtering
- Student performance and course management

## Academic Information

**Institution:** Gisma University of Applied Sciences  
**Module:** B202 Advanced Programming  
**Project:** Java Student Management System  
**Assessment:** Individual Project
