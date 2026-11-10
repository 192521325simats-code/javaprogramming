# Task 05: Derive a Generic Ranking Component from a Principle

**Student Register Number:** 192521325  
**Course:** CSA09 – Programming in Java (CO2)  
**Bloom's Level:** K4 – Analyse  

---

## 1. Problem Context: Inter-College Sports Leaderboard

A sports meet ranks competing teams by:
1. **Points** (higher points first)
2. **Goal Difference** (higher goal difference breaks points ties)
3. **Team Name** (alphabetical A–Z breaks points and goal difference ties)

The design starting point is founded on the fundamental ranking principle:
> *"A ranking is correct only if the comparison used is consistent: if A ranks above B and B ranks above C, then A must rank above C (transitivity), and equal items must compare as equal."*

---

## 2. Design Reasoning: Converting the Principle into Java Architecture

### 2.1 `Comparable<T>` vs Separate `Comparator<T>`
- **Why `Comparator` is superior here:**
  - `Comparable<T>` forces an **intrinsic "natural" ordering** baked directly into the domain class (`Team`). A team does not naturally possess a single universal ordering; one tournament might rank by win percentage, another by head-to-head record, and a third by fair-play disciplinary cards.
  - Implementing `Comparable` couples `Team` permanently to this specific ranking scheme, making it impossible to pass alternative ranking strategies without editing class source code.
  - By using separate `Comparator` strategies, the ranking component satisfies the **Open-Closed Principle (OCP)** and allows multi-domain reusability (ranking `Player`, `College`, `QuizTeam`, etc.).

### 2.2 Expressing Multi-Field Comparisons
Modern Java provides fluent functional combinators that eliminate error-prone manual `if-else` branching:
```java
Comparator<Team> byRank = Comparator
    .comparingInt(Team::getPoints).reversed()                           // 1. Points descending
    .thenComparing(Comparator.comparingInt(Team::getGoalDiff).reversed()) // 2. GoalDiff descending
    .thenComparing(Team::getName);                                      // 3. Name ascending (A-Z)
```

### 2.3 Generics, Bounded Types, and Wildcards
In `Leaderboard<T>`:
```java
class Leaderboard<T> {
    private final List<T> items = new ArrayList<>();
    private final Comparator<? super T> order;

    Leaderboard(Comparator<? super T> order) {
        this.order = order;
    }
}
```
- **Wildcard `Comparator<? super T>` (PECS: Consumer Super):** Allows a comparator defined on a parent class (e.g., `Comparator<Participant>`) to rank instances of a child class (`Team extends Participant`). This maximizes polymorphism and code reuse.
- **Defensive Immutability in `ranking()`:**
  ```java
  List<T> ranking() {
      List<T> copy = new ArrayList<>(this.items);
      copy.sort(this.order);
      return copy;
  }
  ```
  Sorting in place would mutate the internal insertion history of the leaderboard. Returning a new sorted copy preserves encapsulation and prevents side-effects.

---

## 3. Verification & Correctness Inquiry

### 3.1 Why Bisons is Ranked Above Panthers
In the demo execution:
- Output: `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`
- **Analysis:**
  1. `Eagles`: 9 points, +6 goal difference $\rightarrow$ 1st place (highest goal difference among 9-pointers).
  2. `Bisons` and `Panthers`:
     - Points comparison: $9 == 9$ (Tie).
     - Goal difference comparison: $4 == 4$ (Tie).
     - Name comparison: Alphabetical evaluation: `"Bisons".compareTo("Panthers") < 0`. Because "B" precedes "P", Bisons takes precedence.
  3. `Cobras`: 6 points $\rightarrow$ 4th place.

### 3.2 Formal Proof of the Ordering Principle
The comparator satisfies a **strict weak ordering** (transitivity and symmetry):
1. **Transitivity of Equivalence:** If $A \equiv B$ and $B \equiv C$, then:
   - $\text{pts}(A) = \text{pts}(B) = \text{pts}(C)$
   - $\text{gd}(A) = \text{gd}(B) = \text{gd}(C)$
   - $\text{name}(A) = \text{name}(B) = \text{name}(C)$
   Therefore, $A \equiv C$.
2. **Transitivity of Strict Superiority:** If $A > B$ and $B > C$:
   - If $\text{pts}(A) > \text{pts}(B) \ge \text{pts}(C)$, then $\text{pts}(A) > \text{pts}(C)$, so $A > C$.
   - If points are equal, tie-breaker 1 (goal difference) is evaluated, which is transitively ordered in $\mathbb{Z}$.
   - If goal differences are equal, tie-breaker 2 (lexicographical string comparison) is evaluated, which is transitively ordered in the Unicode character space.
   Thus, cyclic preferences ($A > B > C > A$) are mathematically impossible.

### 3.3 Breaking Faulty Comparators (The Subtraction Overflow Bug)
Novice developers frequently write: `(t1, t2) -> t2.points - t1.points`.
- If `t2.points = -2,000,000,000` (penalty) and `t1.points = 100`:
  $$\text{diff} = -2,000,000,000 - 100 = -2,000,000,100 \rightarrow \text{wraps to positive } +2,294,967,196 \text{ in 32-bit signed int}$$
- The negative score is ranked above the positive score!
- **Resolution:** `Comparator.comparingInt()` internally uses `Integer.compare(x, y)` (`(x < y) ? -1 : ((x == y) ? 0 : 1)`), which is immune to integer overflow.

---

## 4. Alternative Concept & Complexity Analysis

### 4.1 Comparator Chain vs Three Stable Sorts vs Intrinsic Comparable

| Approach | Implementation Technique | Time Complexity ($N$ Teams) | Extra Memory Overhead | Ease of Reuse across Domains |
| :--- | :--- | :---: | :---: | :--- |
| **1. Modern Comparator Chain** (Chosen Solution) | Single pass sort (`TimSort`) with composite comparator evaluating tie-breakers in one pass | **$O(N \log N)$** (single sort pass) | $O(N)$ for defensive copy | **Highest:** Pure external strategy pattern; plug-and-play for Players, Colleges, etc. |
| **2. Three Stable Sort Passes** | Perform 3 separate sort passes using `List.sort()` (guaranteed stable TimSort):<br>1. Sort by name<br>2. Sort by goalDiff<br>3. Sort by points | **$3 \times O(N \log N)$** (3 separate full array sorts) | $O(N)$ | **Poor:** Fragile; requires understanding stable sorting invariants; 3x slower execution. |
| **3. Intrinsic `Comparable<Team>`** | `Team implements Comparable<Team>` with hardcoded `compareTo()` | $O(N \log N)$ (single sort pass) | $O(N)$ | **Lowest:** Hardcodes one ordering inside `Team`; cannot rank by alternate metrics without subclassing. |

### 4.2 Reusability Demonstration
In our test suite (`LeaderboardTest.java`), `Leaderboard<Player>` was instantiated with a completely distinct domain (`Player` with goals and assists) without altering a single character of `Leaderboard<T>` code, verifying genuine component reusability.
