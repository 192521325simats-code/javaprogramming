package Task03;

import java.util.HashSet;
import java.util.Set;

/**
 * Attendance program BEFORE completing TODO 1.
 * Demonstrates the bug when equals() is overridden without hashCode().
 */
class StudentBuggy {
    private final String regNo;
    private final String name;

    StudentBuggy(String regNo, String name) {
        this.regNo = regNo;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentBuggy)) return false;
        return regNo.equals(((StudentBuggy) o).regNo);
    }

    // hashCode() is NOT overridden here! Inherits Object.hashCode() (memory address identity hash)

    @Override
    public String toString() {
        return regNo + " " + name;
    }
}

public class AttendanceOriginalBuggy {
    public static void main(String[] args) {
        Set<StudentBuggy> present = new HashSet<>();
        present.add(new StudentBuggy("192311001", "Arun"));  // scanned at Gate 1
        present.add(new StudentBuggy("192311002", "Divya"));
        present.add(new StudentBuggy("192311001", "Arun"));  // scanned again at Gate 3

        System.out.println("Output BEFORE completing TODO 1:");
        System.out.println("Unique students: " + present.size());
    }
}
