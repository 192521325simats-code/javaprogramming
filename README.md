# CSA09 - Programming in Java | Assignment 2 (CO2)

**Course:** CSA09 – Programming in Java  
**Student Register Number:** 192521325  
**Course Faculty Submission Date:** 10-11-2026  
**GitHub Repository:** [https://github.com/192521325simats-code/javaprogramming.git](https://github.com/192521325simats-code/javaprogramming.git)  
**Java Runtime & Compiler:** OpenJDK 27 (build 27.0.1.0.3+1-2, 64-Bit Server VM)  

---

## Executive Summary & Task Results Overview

This repository contains the rigorous implementation, empirical performance benchmarks, algorithmic complexity analyses, and design evaluations for **Assignment 2 (CO2 Coverage: Sessions 15–26)**.

| Task / Question | Topic & Assessed Area | Key Result & Observed Metric | Bloom's Level | Marks |
| :---: | :--- | :--- | :---: | :---: |
| **Task 01 (Q1)** | Building Text Under Load: String, StringBuilder, StringBuffer | `StringBuilder` is **5.54x faster** than String `+` on 20k tickets; avoids allocating 4,000,000 intermediate objects. Per-ticket text recommends `StringBuilder` (thread-confined); shared log uses `StringBuffer` / thread-safe queue. | K4 – Analyse | 6 |
| **Task 02 (Q2)** | When Is ArrayList an Appropriate Engineering Choice? | `ArrayList` random access is **1,401x faster** than `LinkedList` at $N=100,000$. Middle insertion is also **18.1x faster** in ArrayList due to `System.arraycopy`. `ArrayDeque` is optimal for the "Up Next" queue (circular array, $O(1)$ ends). | K4 – Analyse | 4 |
| **Task 03 (Q3)** | Engineer the hashCode–equals Contract | Without `hashCode()`, HashSet recorded **3 duplicates**; with TODO 1 completed, set size is exactly **2** (`Unique students: 2`). Key mutation causes silent memory leaks; TreeSet relies solely on `compareTo()`. | K4 – Analyse | 6 |
| **Task 04 (Q4)** | Choosing a Map for a Pharmacy Inventory | 100k lookups take **6.35 ms** in `HashMap` (0.064 µs/lookup) vs 61.78 ms in `TreeMap`. `LinkedHashMap` (8.99 ms) is the deployed choice: native $O(N)$ insertion report (0.75 ms) and $O(1)$ lookups. Legacy `Hashtable` rejected due to monitor lock contention. | K4 – Analyse | 6 |
| **Task 05 (Q5)** | Generic Ranking Component from a Principle | Executed `Leaderboard<Team>`: `[Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)]`. Bisons beats Panthers via alphabetical tie-breaker. Proved transitivity and exposed subtraction integer overflow bug. Reused across `Player` domain. | K4 – Analyse | 7 |
| **Task 06 (Q6)** | Priority, Order and Fairness in a Queue | Executed `Triage`: `Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5)`. Monotonic counter preserves FIFO among identical severities. Raw heap printing shows partial order. Demonstrated starvation of S5 cases and designed dynamic aging. | K5 – Evaluate | 8 |
| **Task 07 (Q7)** | Day-4 Design Challenge: Diagnose, Redesign, Validate | Diagnosed $O(\log N)$ tree sifting and aging impossibility in `PriorityQueue`. Redesigned as **Multi-Level 5-Bucket Circular Deque**. Achieved **9.96x speedup** (4.28 ms vs 42.63 ms) at $N=100,000$ and $O(1)$ aging preventing starvation. | K5 – Evaluate | 13 |
| **TOTAL** | | **Comprehensive Progressive Commit Submission** | | **50** |

---

## Repository Directory Structure

```
CSA09-192521325-Assignment2/
├── README.md                           <- Master assignment report & guide
├── Task01/                             <- Q1: Railway e-Ticket Text Building Under Load
│   ├── TicketBenchmark.java            <- Benchmark across 3 workloads & multithreading
│   ├── TicketBenchmark.class
│   ├── output.txt                      <- Measured console output
│   └── ANALYSIS.md                     <- Detailed K4 analysis & complexity derivation
├── Task02/                             <- Q2: Music Playlist Editor & Up-Next Queue
│   ├── PlaylistBenchmark.java          <- ArrayList vs LinkedList vs ArrayDeque benchmark
│   ├── PlaylistBenchmark.class
│   ├── output.txt                      <- Measured console output
│   └── ANALYSIS.md                     <- Operating boundaries & memory table
├── Task03/                             <- Q3: Attendance De-duplication & hashCode-equals
│   ├── Attendance.java                 <- Complete the Code (TODO 1 resolved, size 2)
│   ├── AttendanceOriginalBuggy.java    <- Demonstrates size 3 bug before TODO 1
│   ├── HashCodeEqualsInvestigation.java<- 3 designed tests (Mutation, TreeSet anomaly)
│   ├── output.txt                      <- Full test logs
│   └── ANALYSIS.md                     <- Bucket hashing inquiry & contract proof
├── Task04/                             <- Q4: Pharmacy Inventory Map Selection
│   ├── PharmacyInventoryBenchmark.java <- HashMap vs TreeMap vs LinkedHashMap vs BinSearch
│   ├── output.txt                      <- 100k lookup metrics & report timings
│   └── ANALYSIS.md                     <- SLA inquiry, deployment defense & Hashtable analysis
├── Task05/                             <- Q5: Generic Leaderboard & Ordering Principle
│   ├── LeaderboardDemo.java            <- Complete the Code (TODO 1 & 2 resolved)
│   ├── LeaderboardTest.java            <- Transitivity, multi-tier ties, overflow bug test
│   ├── output.txt                      <- Verified output logs
│   └── ANALYSIS.md                     <- Proof of principle, generics & wildcard defense
├── Task06/                             <- Q6: Hospital Emergency Triage Priority Queue
│   ├── Triage.java                     <- Complete the Code (TODO 1 resolved)
│   ├── TriageInvestigation.java        <- 8-patient check, heap printing, growth & aging
│   ├── output.txt                      <- Hand-verified output logs
│   └── ANALYSIS.md                     <- Heap partial-order proof, starvation & fair-rule
└── Task07/                             <- Q7: Day-4 Design Challenge (K5 Evaluate)
    ├── TriageBenchmark.java            <- Original vs Redesigned Bucket Deque benchmark
    ├── output.txt                      <- 100k head-to-head metrics & aging validation
    └── DESIGN_CHALLENGE.md             <- Full 13-Mark Sections A-F engineering defense
```

---

## Instructions to Compile and Run Each Program

All commands should be executed from the root directory of the repository:

### Task 01: String vs StringBuilder vs StringBuffer
```bash
javac Task01/*.java
java Task01.TicketBenchmark
```

### Task 02: ArrayList vs LinkedList vs ArrayDeque
```bash
javac Task02/*.java
java Task02.PlaylistBenchmark
```

### Task 03: hashCode–equals Contract Investigation
```bash
javac Task03/*.java
# Run buggy version before TODO 1 (Output: Unique students: 3)
java Task03.AttendanceOriginalBuggy

# Run completed code after TODO 1 (Expected Output: Unique students: 2)
java Task03.Attendance

# Run full 3-part experiment suite (Reference identity, distinct instances, mutation, TreeSet)
java Task03.HashCodeEqualsInvestigation
```

### Task 04: Hospital Pharmacy Inventory Map Comparison
```bash
javac Task04/*.java
java Task04.PharmacyInventoryBenchmark
```

### Task 05: Generic Leaderboard Component
```bash
javac Task05/*.java
# Run completed code (Expected Output: [Eagles(9,6), Bisons(9,4), Panthers(9,4), Cobras(6,1)])
java Task05.LeaderboardDemo

# Run comprehensive transitivity, ties, and overflow test suite
java Task05.LeaderboardTest
```

### Task 06: Hospital Emergency Triage PriorityQueue
```bash
javac Task06/*.java
# Run completed code (Expected Output: Meena(S1) Kumar(S1) Lata(S2) Ravi(S3) John(S3) Asha(S5))
java Task06.Triage

# Run 8-patient hand check, growth scaling, starvation test, and 5-FIFO queue comparison
java Task06.TriageInvestigation
```

### Task 07: Day-4 Design Challenge (Diagnose, Redesign and Validate)
```bash
javac Task07/*.java
# Run head-to-head comparison between PriorityQueue and Redesigned Multi-Level Bucket Queue
java Task07.TriageBenchmark
```

---

## Git Commit History Verification

Progressive commits were made for each distinct task:
1. `Task01: Implement String, StringBuilder, StringBuffer benchmarks and analysis for railway e-ticket generation`
2. `Task02: Implement ArrayList vs LinkedList vs ArrayDeque performance experiments and analysis`
3. `Task03: Implement hashCode-equals contract investigation and complete Attendance.java`
4. `Task04: Implement Pharmacy Inventory Map benchmark (HashMap, TreeMap, LinkedHashMap) and binary search comparison`
5. `Task05: Implement generic Leaderboard component, comparator chain, and transitivity test suite`
6. `Task06: Implement PriorityQueue triage system, arrival counter ordering, and aging inquiry`
7. `Task07: Complete Day-4 Design Challenge with multi-level bucket queue redesign and validation`
8. `Docs: Complete comprehensive assignment README and documentation report`
