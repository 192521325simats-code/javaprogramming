# Task 03: Engineer the hashCode–equals Contract

**Student Register Number:** 192521325  
**Course:** CSA09 – Programming in Java (CO2)  
**Bloom's Level:** K4 – Analyse  

---

## 1. Problem Context: Event Attendance De-duplication

College fest organizers record attendance across multiple entry gates. The same student may scan their ID badge multiple times at different gates. Scans are stored in a `HashSet<Student>` to keep only unique attendees.

A junior developer implemented `Student` with:
```java
public boolean equals(Object o) { 
    if (!(o instanceof Student)) return false; 
    return regNo.equals(((Student) o).regNo); 
} 
// hashCode() is NOT overridden!
```

---

## 2. Experimental Execution: Before and After TODO 1

### 2.1 Output Comparison

#### Before Completing TODO 1 (Buggy State):
```
Output BEFORE completing TODO 1:
Unique students: 3
```
*Observation:* Even though `192311001` (Arun) was scanned twice, the set recorded **3 students** instead of 2.

#### After Completing TODO 1 (Corrected State):
```
Unique students: 2
```
*Observation:* The duplicate scan for `192311001` was correctly identified and rejected. The set size is exactly **2** (`Arun` and `Divya`).

---

## 3. The 3 Deliberately Designed Experimental Tests

| Test | Objective | Design & Scenario | Predicted Size | Observed Size | Verdict & Analysis |
| :--- | :--- | :--- | :---: | :---: | :--- |
| **Test 1** | Apparent Success | Pass the **exact same Java object reference** (`s1`) to Gate 1 and Gate 2. | **1** | **1** | **PASSED:** Because it is the same memory reference, `System.identityHashCode(s1)` produces the same bucket index, and `s1 == o` returns true immediately. This masks the missing `hashCode()` bug. |
| **Test 2** | Duplicate Exposure | Create **two separate `new StudentBuggy("192311001", "Arun")` instances**. | **2** | **2** | **BUG EXPOSED:** Each `new` allocation has a different memory address, giving distinct `identityHashCode()` values. They map to different buckets, so `equals()` is **never invoked**. Duplicates enter the set! |
| **Test 3A** | Key Mutation After Insertion | Insert `MutableStudent("192311010", "Rahul")`, then mutate field `regNo = "192311099"`. | **1 (Corrupt)** | **1 (Corrupt)** | **CORRUPTION:** `set.contains()` returns `false`! Attempting `set.remove()` fails (`false`), permanently leaking memory because the lookup searches bucket $H(\text{new})$, but the item sits in bucket $H(\text{old})$. |
| **Test 3B** | TreeSet Inconsistent with Equals | Instantiate `TreeSet<Student>` with a `Comparator` comparing `toString().length()`. | **1** | **1** | **ANOMALY:** `TreeSet` relies strictly on `compareTo() == 0`, ignoring `equals()`. Because `"192311001 Arun"` and `"192311002 Bala"` both have length 14, Bala is rejected as a duplicate! |

---

## 4. Deep-Dive Inquiry: The hashCode–equals Mechanics

### 4.1 Why Duplicates Appeared
The Java language specification states the **hashCode–equals contract**:
> *If two objects are equal according to the `equals(Object)` method, then calling `hashCode()` on each of the two objects must produce the same integer result.*

When `hashCode()` is omitted, `Student` inherits `java.lang.Object.hashCode()`, which defaults to the object's identity hash code (derived from its memory address). For two distinct objects `s1` and `s2` with the same `regNo`:
- `s1.equals(s2)` evaluates to `true`.
- But `s1.hashCode() != s2.hashCode()`.
Because their hash codes differ, `HashSet` routes them to completely different internal hash buckets. As a result, `equals()` is never even checked, and both duplicates coexist inside the set.

### 4.2 How HashSet Locates an Element
1. **Hash Calculation:** The element's hash code is scrambled using HotSpot's supplemental spreading function:
   $$h = \text{key.hashCode()} \oplus (h \ggg 16)$$
2. **Bucket Mapping:** The bucket array index $i$ is calculated via bitwise AND:
   $$i = (n - 1) \ \& \ h \quad (\text{where } n = 2^k \text{ is the bucket table length})$$
3. **Bucket Inspection:**
   - If bucket $i$ is empty: The element is inserted as a new entry.
   - If bucket $i$ contains entries (collision bin): `HashSet` iterates through the nodes in the bucket. For each node, it evaluates:
     $$\text{if } (p.\text{hash} == \text{hash} \ \&\& \ (p.\text{key} == \text{key} \ || \ (key \ne \text{null} \ \&\& \ \text{key.equals}(p.\text{key}))))$$
   - Notice the short-circuit: **`equals()` is only evaluated if the cached 32-bit hash codes match first!** If the hash codes differ, `equals()` is completely bypassed.

### 4.3 What Happens When a Key Field is Mutated After Insertion
If `regNo` is modified after insertion:
1. When `contains(student)` is called, Java re-evaluates `student.hashCode()` using the *new* `regNo`.
2. This computes a completely new bucket index $j \ne i$.
3. Java inspects bucket $j$, finds it empty (or holding other unrelated keys), and returns `false`.
4. Even worse, `remove(student)` cannot locate the element, leaving the corrupted node stranded in bucket $i$. This causes severe **silent memory leaks** in long-running services.
5. **Rule:** Fields contributing to `hashCode()` and `equals()` must always be declared **`private final`** (immutable).

### 4.4 How to Correct the Student Class
To guarantee contract conformance:
```java
class Student {
    private final String regNo;
    private final String name;

    Student(String regNo, String name) {
        this.regNo = Objects.requireNonNull(regNo, "regNo cannot be null");
        this.name = Objects.requireNonNull(name, "name cannot be null");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return Objects.equals(regNo, student.regNo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(regNo);
    }
}
```

---

## 5. Alternative Concept & Complexity Analysis

### 5.1 Comparing De-duplication Approaches for $N$ Scans ($U$ Unique Students)

| Approach | Implementation Structure | Time Complexity (Per Scan) | Total Time ($N$ Scans) | Space Complexity ($U$ Unique Entries) | Notes on Ordering & Contract |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **`HashSet<Student>`** (Current Corrected) | Hash table with separate chaining (treeified to red-black tree at bin count $\ge 8$) | **$O(1)$ Average**<br>$O(N)$ Worst (adversarial collisions) | **$O(N)$** | $O(U)$<br>Table array + `Node` objects (~32 bytes/entry) | Requires consistent `hashCode()` and `equals()`. No order guarantee. |
| **`TreeSet<Student>`** with Comparator | Self-balancing Red-Black Tree (`TreeMap` backing) | **$O(\log U)$ Worst & Average** | **$O(N \log U)$** | $O(U)$<br>`Entry` objects (left, right, parent, color ~40 bytes/entry) | **Does not use `hashCode()` or `equals()`!** Equality is determined solely by `comparator.compare(a, b) == 0`. Automatically keeps students sorted. |
| **`HashMap<String, Student>`** | Hash table keyed directly by immutable `String regNo` | **$O(1)$ Average** | **$O(N)$** | $O(U)$<br>Key + Value slots (~36 bytes/entry) | Offloads hashing to Java's built-in `String.hashCode()`. Avoids custom class hashing pitfalls. |

### 5.2 Key Takeaways
- For pure high-throughput de-duplication at turnstiles, **`HashSet<Student>`** (or `HashMap<String, Student>`) is optimal because each scan processes in $O(1)$ average time regardless of the total attendance size.
- If attendance logs must simultaneously be sorted for immediate alphabetical/roll-number reporting, **`TreeSet<Student>`** eliminates a separate post-sort step at the trade-off of $O(\log U)$ time per scan.
