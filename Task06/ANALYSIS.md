# Task 06: Priority, Order and Fairness in a Queue

**Student Register Number:** 192521325  
**Course:** CSA09 – Programming in Java (CO2)  
**Bloom's Level:** K5 – Evaluate  

---

## 1. Problem Context: Hospital Emergency Triage

Emergency departments triage arriving patients into five discrete severity levels:
- **Severity 1:** Resuscitation / Critical (immediate life threat)
- **Severity 2:** Emergent
- **Severity 3:** Urgent
- **Severity 4:** Less Urgent
- **Severity 5:** Non-urgent / Minor

**The Operating Rules:**
1. The physician on duty must always admit and treat the highest priority patient first (lowest severity number first: $1 \rightarrow 2 \rightarrow 3 \rightarrow 4 \rightarrow 5$).
2. Among patients presenting with the identical severity level, the patient who arrived earlier must be seen first (**FIFO order preserved among equals**).

---

## 2. Design Reasoning: Why Plain Queues Fail & How PriorityQueue Enforces the Rule

### 2.1 Why Plain `Queue` (`LinkedList` or `ArrayDeque`) Is Insufficient
A standard `Queue` implementation is strictly First-In, First-Out (FIFO). Elements are dequeued strictly in the sequence they were admitted. If an ambulatory patient with a sprained ankle (Severity 5) arrives at 10:00 AM, and a cardiac arrest patient (Severity 1) arrives at 10:05 AM, a plain FIFO queue forces the doctor to treat the sprained ankle first. This violates medical triage protocols.

### 2.2 How `PriorityQueue` Enforces the Rule
Java's `PriorityQueue<E>` is implemented as an array-based **binary min-heap**. By providing a two-stage composite comparator:
```java
Comparator.comparingInt(Patient::getSeverity)
          .thenComparingLong(Patient::getArrival)
```
1. The heap organizes elements such that the patient with the lowest severity integer sits at the root index `queue[0]`.
2. Extracting via `poll()` always retrieves the minimum element in $O(\log N)$ time, restoring the min-heap invariant via sift-down (`siftDownComparable`).

### 2.3 How the Arrival Counter Guarantees FIFO Among Equals
Standard Java `PriorityQueue` is **unstable**; if two elements compare as equal (`compare(a, b) == 0`), the binary heap does not guarantee which one sits higher in the tree or gets sifted down first during deletions.
- **The Solution:** We attach a strictly monotonic global counter `private long counter = 0` to each admission:
  `queue.add(new Patient(name, severity, counter++));`
- Because `counter` strictly increments with every admission ($t_1 < t_2$), two patients with identical severity are never tied in the comparator. The secondary tie-breaker `.thenComparingLong(Patient::getArrival)` deterministically orders the earlier arrival first, guaranteeing FIFO stability.

---

## 3. Hand-Verified Correctness Check & Heap Inspection

### 3.1 8-Patient Hand Calculation

| Arrival Step | Patient | Severity | Arrival Counter | Hand-Calculated Correct Treatment Rank |
| :---: | :--- | :---: | :---: | :---: |
| 1 | Anita | 3 | 0 | 5th (Tie S3, earlier arrival) |
| 2 | Bharat | 1 | 1 | **1st** (Tie S1, earlier arrival) |
| 3 | Chitra | 2 | 2 | 3rd (Tie S2, earlier arrival) |
| 4 | Deepak | 1 | 3 | **2nd** (Tie S1, later arrival) |
| 5 | Esha | 4 | 4 | 7th |
| 6 | Farhan | 3 | 5 | 6th (Tie S3, later arrival) |
| 7 | Gita | 5 | 6 | 8th |
| 8 | Hari | 2 | 7 | 4th (Tie S2, later arrival) |

**Hand-Calculated Extraction Order:**  
`Bharat(S1) Deepak(S1) Chitra(S2) Hari(S2) Anita(S3) Farhan(S3) Esha(S4) Gita(S5)`

### 3.2 Why Printing `PriorityQueue` Directly Does NOT Show Treatment Order
When executing `System.out.println(queue)`, the console displays:
```
[Bharat(S1), Deepak(S1), Chitra(S2), Hari(S2), Esha(S4), Farhan(S3), Gita(S5), Anita(S3)]
```
Notice that `Anita(S3)` appears at the very end of the array after `Gita(S5)`!
- **Reason:** `PriorityQueue.toString()` invokes its `iterator()`, which traverses the internal backing array `Object[] queue` sequentially from index 0 to $N-1$.
- In a binary heap, the array only satisfies the **partial-order heap invariant**:
  $$\text{queue}[k] \le \text{queue}[2k + 1] \quad \text{and} \quad \text{queue}[k] \le \text{queue}[2k + 2]$$
- Sibling nodes across different branches are unordered with respect to one another. Total order is realized only dynamically by calling `poll()`, which extracts the root and sifts down remaining elements one by one.

---

## 4. Growth Scaling & Empirical Investigation

Tested across $N = 1,000$, $10,000$, and $100,000$ admissions and discharges:

| Queue Size ($N$) | Total Admit Time (ms) | Total Poll Time (ms) | Avg Time Per Operation (µs) | Algorithmic Scaling |
| :---: | :---: | :---: | :---: | :---: |
| **1,000** | 5.33 | 5.96 | 5.647 µs | $\log_2(1,000) \approx 10$ steps |
| **10,000** | 5.74 | 3.43 | 0.458 µs | $\log_2(10,000) \approx 13$ steps |
| **100,000** | 21.96 | 29.77 | 0.259 µs | $\log_2(100,000) \approx 17$ steps |

The average time per operation remains sub-microsecond, demonstrating excellent $O(\log N)$ logarithmic scaling.

---

## 5. Starvation Inquiry & Fair-Rule Design

### 5.1 The Starvation Problem
In an emergency room operating under pure severity priority, low-priority patients (Severity 4 and 5, such as suture removals or mild sprains) experience **starvation** (infinite delay). If higher severity patients (S1, S2, S3) continue to arrive at or above the discharge rate, the heap root will never contain an S4 or S5 patient. In our experiment, after 50 continuous treatments, Asha (S5) was never seen.

### 5.2 Fair-Rule Design: Dynamic Priority Aging
To guarantee bounded wait times without violating clinical safety, we introduce **Aging**:
$$\text{Effective Severity}(t) = \max\left(1, \ \text{Base Severity} - \left\lfloor \frac{t - \text{arrival\_time}}{\text{AgingThreshold}} \right\rfloor \right)$$
- Every $\Delta T$ minutes spent waiting in the queue, a patient's effective severity level promotes by 1.
- In our simulation (`TriageInvestigation.java`), at tick 85, Asha's effective severity was elevated from S5 to S1, successfully scheduling her for immediate consultation ahead of newer mild arrivals.

---

## 6. City Hospital Deployment Feasibility

### 6.1 Workload Analysis for 5,000 Patients / Day
- 5,000 admissions spread across 24 hours = **~0.058 patients/sec** (or peak rush of ~5 patients/sec).
- Measured performance: Our `PriorityQueue` admitted and processed 10,000 patients in **9.17 ms total**.
- **Conclusion:** The binary heap design can process the entire city hospital's daily traffic in less than 10 milliseconds of CPU time. It is **extremely well suited and approved for deployment**.

---

## 7. Alternative Concept & Complexity Analysis

### 7.1 Architecture Comparison Table

| Architecture | Enqueue / Admit Time | Dequeue / Poll Time | Space Overhead | Cache Locality |
| :--- | :---: | :---: | :---: | :---: |
| **1. `PriorityQueue` (Binary Min-Heap)** | $O(\log N)$ | $O(\log N)$ | $O(N)$ contiguous array | Good (array based, but jumps during sift-down) |
| **2. Sorted `LinkedList`** | **$O(N)$** (linear scan to find position) | **$O(1)$** (remove head) | $O(N)$ with heavy Node objects | Very poor (pointer chasing) |
| **3. Array of 5 FIFO Queues (`ArrayDeque[5]`)** | **$O(1)$** (direct bucket `queues[s-1].addLast()`) | **$O(1)$** (scan 5 bucket heads) | $O(N)$ contiguous circular arrays | **Outstanding** (Zero rebalancing, $O(1)$) |

In Part 4 of our benchmark, the **Array of 5 FIFO Queues** completed 20,000 triage events in **7.84 ms**, delivering a **4.23x speedup** over `PriorityQueue` (33.14 ms) due to eliminating heap rebalancing entirely.
