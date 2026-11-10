package Task03;

import java.util.*;

/**
 * Task03 - Empirical Investigation of the hashCode-equals Contract
 * 
 * Conducts 3 deliberate experiments:
 *  Test 1: Scans where HashSet appears to work correctly despite missing hashCode()
 *  Test 2: Scans designed to expose duplicate entries
 *  Test 3: Mutation of hash field after insertion & TreeSet comparison inconsistency
 * 
 * Student Register Number: 192521325
 */
public class HashCodeEqualsInvestigation {

    static class MutableStudent {
        String regNo;
        String name;

        MutableStudent(String regNo, String name) {
            this.regNo = regNo;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MutableStudent)) return false;
            return Objects.equals(regNo, ((MutableStudent) o).regNo);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(regNo);
        }

        @Override
        public String toString() {
            return regNo + " (" + name + ")";
        }
    }

    public static void runTest1_ApparentSuccess() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("TEST 1: Set of scans where HashSet APPEARS to work correctly without hashCode()");
        System.out.println("Scenario: Same Student object reference scanned at Gate 1 and Gate 2.");
        System.out.println("Prediction: Size will be 1, because same instance shares identical identity hashCode.");
        
        Set<StudentBuggy> set = new HashSet<>();
        StudentBuggy s1 = new StudentBuggy("192311005", "Priya");
        set.add(s1); // Gate 1
        set.add(s1); // Gate 2 (re-scan of same reference)

        System.out.println("Observed Set Size: " + set.size());
        System.out.println("Result: " + (set.size() == 1 ? "PASSED (Appeared correct due to reference identity)" : "FAILED"));
    }

    public static void runTest2_DuplicateExposed() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("TEST 2: Deliberately designed to expose duplicates (Separate instances)");
        System.out.println("Scenario: Two distinct StudentBuggy objects representing same student '192311001'.");
        System.out.println("Prediction: Size will be 2 (Duplicate present!), because default System.identityHashCode() assigns them to different buckets.");

        Set<StudentBuggy> set = new HashSet<>();
        set.add(new StudentBuggy("192311001", "Arun"));
        set.add(new StudentBuggy("192311001", "Arun"));

        System.out.println("Observed Set Size: " + set.size());
        System.out.println("Result: " + (set.size() == 2 ? "EXPOSED BUG (Duplicate allowed in Set!)" : "UNEXPECTED"));
    }

    public static void runTest3_MutationAndTreeSet() {
        System.out.println("--------------------------------------------------------------------------");
        System.out.println("TEST 3A: Mutating a key field after insertion into HashSet");
        System.out.println("Scenario: Add MutableStudent('192311010', 'Rahul'), then mutate regNo to '192311099'.");
        System.out.println("Prediction: set.contains(student) will return FALSE even though student is inside the set! Element becomes an unremovable zombie (memory leak).");

        Set<MutableStudent> set = new HashSet<>();
        MutableStudent ms = new MutableStudent("192311010", "Rahul");
        set.add(ms);
        System.out.println("Before mutation - contains('192311010'): " + set.contains(ms));

        // Mutate the field used in hashCode() and equals()
        ms.regNo = "192311099";
        boolean containsAfter = set.contains(ms);
        System.out.println("After mutation  - contains('192311099'): " + containsAfter);
        System.out.println("Set size: " + set.size() + ", Elements: " + set);
        System.out.println("Attempt remove: " + set.remove(ms) + ", Size after failed remove: " + set.size());

        System.out.println();
        System.out.println("TEST 3B: TreeSet with compareTo inconsistent with equals");
        System.out.println("Scenario: Comparator compares only name length, while equals compares regNo.");
        Comparator<Student> lengthComparator = Comparator.comparingInt(s -> s.toString().length());
        TreeSet<Student> treeSet = new TreeSet<>(lengthComparator);
        Student a = new Student("192311001", "Arun");     // "192311001 Arun" (len 14)
        Student b = new Student("192311002", "Bala");     // "192311002 Bala" (len 14)
        treeSet.add(a);
        treeSet.add(b); // len 14 == len 14 -> compare returns 0! Rejected as duplicate!
        System.out.println("TreeSet size with inconsistent comparator: " + treeSet.size() + " (Expected 2, got 1!)");
        System.out.println("Elements in TreeSet: " + treeSet);
    }

    public static void main(String[] args) {
        System.out.println("=== Q3 INVESTIGATION: hashCode-equals CONTRACT TESTS ===");
        runTest1_ApparentSuccess();
        runTest2_DuplicateExposed();
        runTest3_MutationAndTreeSet();
    }
}
