/*
 * B202 Advanced Programming - Student Management System
 *
 * One-file Java 21 console application.
 * OOP: encapsulation, abstraction, inheritance, overriding, polymorphism.
 * Data: entered manually; saved automatically to students.txt in the run folder.
 *
 * Compile: javac StudentManagementSystem.java
 * Run:     java StudentManagementSystem
 * Test:    java StudentManagementSystem --self-test
 *
 * All classes intentionally live in one .java file for easy first-time setup.
 * For a larger project, each class could be moved into its own file.
 */

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class StudentManagementSystem {
    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        if (args.length == 1 && "--self-test".equals(args[0])) {
            runSelfTests();
            return;
        }

        StudentManager manager = new StudentManager(Path.of("students.txt"));
        try {
            manager.load();
        } catch (IOException | IllegalArgumentException exception) {
            System.out.println("Could not load saved records: " + exception.getMessage());
            System.out.println("The program will continue with an empty list. Fix the file before saving.");
            return; // Protect existing data from accidental replacement.
        }

        System.out.println("Welcome! Loaded " + manager.count() + " student record(s).");
        while (true) {
            showMenu();
            int choice = readInt("Select an option (1-7): ", 1, 7);
            switch (choice) {
                case 1 -> addStudent(manager);
                case 2 -> displayStudents(manager);
                case 3 -> searchStudent(manager);
                case 4 -> updateStudent(manager);
                case 5 -> deleteStudent(manager);
                case 6 -> showStatistics(manager);
                case 7 -> {
                    System.out.println("Goodbye. Your changes have been saved.");
                    return;
                }
                default -> throw new IllegalStateException("Unexpected menu choice");
            }
        }
    }

    private static void showMenu() {
        System.out.println("\n====================================");
        System.out.println("      STUDENT MANAGEMENT SYSTEM");
        System.out.println("====================================");
        System.out.println("1. Add a student");
        System.out.println("2. View all students");
        System.out.println("3. Search by student ID");
        System.out.println("4. Update a student");
        System.out.println("5. Delete a student");
        System.out.println("6. Student statistics");
        System.out.println("7. Exit");
    }

    private static void addStudent(StudentManager manager) {
        String id = readId("Student ID (letters, digits, - or _): ");
        if (manager.find(id).isPresent()) {
            System.out.println("This student ID already exists.");
            return;
        }
        String name = readNonEmpty("Student name: ");
        String course = readNonEmpty("Course name: ");
        int age = readInt("Age (10-120): ", 10, 120);
        int category = readInt("Category: 1 = Undergraduate, 2 = Postgraduate: ", 1, 2);
        Student student = category == 1
                ? new UndergraduateStudent(id, name, course, age)
                : new PostgraduateStudent(id, name, course, age);
        manager.add(student);
        saveAndReport(manager, "Student added.");
    }

    private static void displayStudents(StudentManager manager) {
        List<Student> students = manager.all();
        if (students.isEmpty()) {
            System.out.println("No student records available.");
            return;
        }
        System.out.printf("%-14s %-22s %-23s %-5s %s%n", "ID", "NAME", "COURSE", "AGE", "LEVEL");
        System.out.println("---------------------------------------------------------------------------------");
        for (Student student : students) {
            // Runtime polymorphism: level() is selected from the actual student subtype.
            System.out.printf("%-14s %-22s %-23s %-5d %s%n",
                    shorten(student.getId(), 14), shorten(student.getName(), 22),
                    shorten(student.getCourse(), 23), student.getAge(), student.level());
        }
        System.out.println("Total: " + students.size());
    }

    private static void searchStudent(StudentManager manager) {
        String id = readId("Enter student ID: ");
        Optional<Student> found = manager.find(id);
        if (found.isPresent()) {
            System.out.println(found.get().details());
        } else {
            System.out.println("Student not found.");
        }
    }

    private static void updateStudent(StudentManager manager) {
        String id = readId("Enter student ID to update: ");
        Optional<Student> existing = manager.find(id);
        if (existing.isEmpty()) {
            System.out.println("Student not found.");
            return;
        }
        Student student = existing.get();
        System.out.println("Existing: " + student.details());
        String newName = readOptional("New name (Enter to keep current): ");
        String newCourse = readOptional("New course (Enter to keep current): ");
        String newAge = readOptional("New age (Enter to keep current): ");
        Integer parsedAge = null;
        if (!newAge.isEmpty()) {
            try {
                parsedAge = Integer.parseInt(newAge);
                if (parsedAge < 10 || parsedAge > 120) {
                    System.out.println("Age must be between 10 and 120. No changes made.");
                    return;
                }
            } catch (NumberFormatException exception) {
                System.out.println("Age must be a whole number. No changes made.");
                return;
            }
        }
        if (!newName.isEmpty()) student.setName(newName);
        if (!newCourse.isEmpty()) student.setCourse(newCourse);
        if (parsedAge != null) student.setAge(parsedAge);
        saveAndReport(manager, "Student updated.");
    }

    private static void deleteStudent(StudentManager manager) {
        String id = readId("Enter student ID to delete: ");
        Optional<Student> existing = manager.find(id);
        if (existing.isEmpty()) {
            System.out.println("Student not found.");
            return;
        }
        System.out.println("Selected: " + existing.get().details());
        String answer = readOptional("Confirm deletion (yes/no): ");
        if (answer.equalsIgnoreCase("yes")) {
            manager.remove(id);
            saveAndReport(manager, "Student deleted.");
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void showStatistics(StudentManager manager) {
        long undergrads = manager.all().stream().filter(s -> s instanceof UndergraduateStudent).count();
        long postgrads = manager.all().stream().filter(s -> s instanceof PostgraduateStudent).count();
        System.out.println("Total students: " + manager.count());
        System.out.println("Undergraduate: " + undergrads);
        System.out.println("Postgraduate: " + postgrads);
    }

    private static void saveAndReport(StudentManager manager, String successMessage) {
        try {
            manager.save();
            System.out.println(successMessage + " Saved to students.txt.");
        } catch (IOException exception) {
            System.out.println("Change made in memory, but saving FAILED: " + exception.getMessage());
            System.out.println("Do not close the application until you resolve the issue.");
        }
    }

    private static String readId(String prompt) {
        while (true) {
            String input = readNonEmpty(prompt);
            if (input.matches("[A-Za-z0-9_-]{1,20}")) return input;
            System.out.println("Use 1-20 letters, digits, hyphens, or underscores only.");
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            String value = readOptional(prompt);
            if (!value.isEmpty()) return value;
            System.out.println("This value cannot be empty.");
        }
    }

    private static String readOptional(String prompt) {
        System.out.print(prompt);
        if (!INPUT.hasNextLine()) {
            System.out.println("\nInput ended. Closing application.");
            System.exit(0);
        }
        return INPUT.nextLine().trim();
    }

    private static int readInt(String prompt, int minimum, int maximum) {
        while (true) {
            try {
                int value = Integer.parseInt(readNonEmpty(prompt));
                if (value >= minimum && value <= maximum) return value;
            } catch (NumberFormatException ignored) {
                // Show a friendly error and try again.
            }
            System.out.println("Enter a whole number between " + minimum + " and " + maximum + ".");
        }
    }

    private static String shorten(String value, int width) {
        return value.length() <= width ? value : value.substring(0, width - 3) + "...";
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError("Test FAILED: " + message);
        System.out.println("PASS: " + message);
    }

    private static void runSelfTests() {
        Path directory = null;
        try {
            directory = Files.createTempDirectory("b202-students-test-");
            Path file = directory.resolve("test-students.txt");
            StudentManager students = new StudentManager(file);
            check(students.count() == 0, "starts with an empty list");
            check(students.add(new UndergraduateStudent("S001", "Alice", "Computing", 20)),
                    "adds an undergraduate");
            check(students.add(new PostgraduateStudent("S002", "Bob", "Data Science", 24)),
                    "adds a postgraduate");
            check(!students.add(new UndergraduateStudent("S001", "Duplicate", "Test", 18)),
                    "rejects duplicate student ID");
            check(students.find("s001").isPresent(), "finds IDs without case sensitivity");
            Student bob = students.find("S002").orElseThrow();
            bob.setCourse("Artificial Intelligence");
            check(bob.getCourse().equals("Artificial Intelligence"), "updates student data");
            check(students.all().get(0).level().equals("Undergraduate"), "polymorphic level() works");
            students.save();
            StudentManager reloaded = new StudentManager(file);
            reloaded.load();
            check(reloaded.count() == 2, "persists student records to disk");
            check(reloaded.find("S002").orElseThrow().getCourse().equals("Artificial Intelligence"),
                    "loads updated records accurately");
            check(reloaded.remove("S001"), "deletes an existing student");
            check(!reloaded.remove("NOT_FOUND"), "handles missing student deletion");
            System.out.println("\nALL 11 TESTS PASSED.");
        } catch (Exception exception) {
            System.err.println("Self-test FAILED: " + exception.getMessage());
            System.exit(1);
        } finally {
            if (directory != null) {
                try {
                    Files.deleteIfExists(directory.resolve("test-students.txt"));
                    Files.deleteIfExists(directory);
                } catch (IOException ignored) { }
            }
        }
    }
}

// ABSTRACTION: Student defines the common properties and an abstract level() method.
abstract class Student {
    private final String id;
    private String name;
    private String course;
    private int age;

    protected Student(String id, String name, String course, int age) {
        if (id == null || !id.matches("[A-Za-z0-9_-]{1,20}")) {
            throw new IllegalArgumentException("Invalid student ID");
        }
        this.id = id;
        setName(name);
        setCourse(course);
        setAge(age);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCourse() { return course; }
    public int getAge() { return age; }

    // ENCAPSULATION: private fields are modified through validated setters.
    public void setName(String value) { name = requireText(value, "Name"); }
    public void setCourse(String value) { course = requireText(value, "Course"); }
    public void setAge(int value) {
        if (value < 10 || value > 120) throw new IllegalArgumentException("Age must be 10-120");
        age = value;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " cannot be empty");
        }
        if (value.trim().length() > 100) throw new IllegalArgumentException(label + " is too long");
        return value.trim();
    }

    public abstract String level();

    public String details() {
        return "ID: " + id + " | Name: " + name + " | Course: " + course
                + " | Age: " + age + " | Level: " + level();
    }
}

// INHERITANCE and METHOD OVERRIDING: student subtypes extend Student.
final class UndergraduateStudent extends Student {
    public UndergraduateStudent(String id, String name, String course, int age) {
        super(id, name, course, age);
    }
    @Override public String level() { return "Undergraduate"; }
}

final class PostgraduateStudent extends Student {
    public PostgraduateStudent(String id, String name, String course, int age) {
        super(id, name, course, age);
    }
    @Override public String level() { return "Postgraduate"; }
}

// Manages student records and file persistence using the Java standard library only.
final class StudentManager {
    private final List<Student> students = new ArrayList<>();
    private final Path dataFile;

    public StudentManager(Path dataFile) { this.dataFile = dataFile; }
    public int count() { return students.size(); }

    public List<Student> all() {
        return students.stream().sorted(Comparator.comparing(Student::getId)).toList();
    }

    public Optional<Student> find(String id) {
        return students.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst();
    }

    public boolean add(Student student) {
        if (find(student.getId()).isPresent()) return false;
        students.add(student);
        return true;
    }

    public boolean remove(String id) {
        return students.removeIf(s -> s.getId().equalsIgnoreCase(id));
    }

    public void save() throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("# type|id|name(base64)|course(base64)|age");
        for (Student s : all()) {
            String type = s instanceof UndergraduateStudent ? "U" : "P";
            lines.add(type + "|" + s.getId() + "|" + encode(s.getName()) + "|"
                    + encode(s.getCourse()) + "|" + s.getAge());
        }
        Files.write(dataFile, lines, StandardCharsets.UTF_8);
    }

    public void load() throws IOException {
        if (!Files.exists(dataFile)) return;
        List<Student> readStudents = new ArrayList<>();
        int lineNumber = 0;
        for (String line : Files.readAllLines(dataFile, StandardCharsets.UTF_8)) {
            lineNumber++;
            if (line.isBlank() || line.startsWith("#")) continue;
            try {
                String[] columns = line.split("\\|", -1);
                if (columns.length != 5) throw new IllegalArgumentException("Expected five fields");
                String id = columns[1];
                String name = decode(columns[2]);
                String course = decode(columns[3]);
                int age = Integer.parseInt(columns[4]);
                Student student = switch (columns[0]) {
                    case "U" -> new UndergraduateStudent(id, name, course, age);
                    case "P" -> new PostgraduateStudent(id, name, course, age);
                    default -> throw new IllegalArgumentException("Unknown student type");
                };
                if (readStudents.stream().anyMatch(s -> s.getId().equalsIgnoreCase(id))) {
                    throw new IllegalArgumentException("Duplicate student ID");
                }
                readStudents.add(student);
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException("Invalid students.txt line " + lineNumber
                        + ": " + exception.getMessage(), exception);
            }
        }
        students.clear();
        students.addAll(readStudents);
    }

    private static String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
    private static String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
