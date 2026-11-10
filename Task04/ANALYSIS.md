# Task 04: Choosing a Map for a Pharmacy Inventory

**Student Register Number:** 192521325  
**Course:** CSA09 – Programming in Java (CO2)  
**Bloom's Level:** K4 – Analyse  

---

## 1. Problem Context: Hospital Pharmacy Stock Lookup

A hospital pharmacy tracks medicine stock keyed by unique medicine codes (e.g., `MED-042918`).
- **Real-Time Operational Workload:** Pharmacists perform hundreds to thousands of stock lookups per day during active dispensing.
- **Batch Evening Reporting:** Every evening, the system must generate two separate reports:
  1. **Report 1:** All medicines in alphabetical code order (for annual stocktaking and supplier ordering).
  2. **Report 2:** All medicines in the chronological order they were added to the inventory (for batch traceability and audit logs).

---

## 2. Theoretical Analysis Before Implementation

### 2.1 Theoretical Cost Breakdown

| Map Implementation | Underlying Data Structure | `put(k, v)` | `get(k)` | Key / Value Iteration | Natural Ordering Guarantee |
| :--- | :--- | :---: | :---: | :---: | :--- |
| **`HashMap`** | Hash table with separate chaining bins | $O(1)$ amortized | $O(1)$ average | $O(\text{Capacity} + N)$ | **None.** Pseudo-random hash distribution; order scrambles upon resizing. |
| **`TreeMap`** | Self-balancing Red-Black binary search tree | $O(\log N)$ | $O(\log N)$ | $O(N)$ (in-order tree traversal) | **Natural key order** (`Comparable` or `Comparator`). Perfect for Report 1. |
| **`LinkedHashMap`** | Hash table + doubly-linked list running through all entries | $O(1)$ amortized | $O(1)$ average | $O(N)$ (following entry list) | **Insertion order** (or access order). Perfect for Report 2. |

### 2.2 Extra Work If the "Wrong" Map is Selected
- **If `HashMap` is chosen:**
  - *For Report 1 (Code order):* The entire collection must be extracted into an `ArrayList` and sorted via `Collections.sort()` ($O(N \log N)$ time and $O(N)$ extra memory).
  - *For Report 2 (Insertion order):* Impossible without maintaining an external sequence list or injecting an auto-incrementing integer/timestamp field into the `Medicine` object.
- **If `TreeMap` is chosen:**
  - *For Report 1:* Produced natively with zero overhead.
  - *For Report 2:* Impossible because `TreeMap` permanently sorts entries by code; insertion timestamps would have to be stored and extracted into a separate list and re-sorted.
- **If `LinkedHashMap` is chosen:**
  - *For Report 2:* Produced natively in $O(N)$ time.
  - *For Report 1:* Requires a one-time evening sort ($O(N \log N)$), but daytime lookup remains $O(1)$.

---

## 3. Measured Experimental Evidence

Tested across three database scales ($N = 1,000$, $10,000$, and $100,000$ medicines) under OpenJDK 27 with 100,000 random lookup queries.

| Inventory Size | Structure | Put Time (ms) | 100,000 Lookups (ms) | Single Lookup Latency | Rep 1: Code Order | Rep 2: Insertion Order |
| :--- | :--- | :---: | :---: | :---: | :---: | :---: |
| **$N = 1,000$** | HashMap<br>TreeMap<br>LinkedHashMap<br>Sorted Array | 0.42<br>3.23<br>0.40<br>— | 4.66<br>14.28<br>4.65<br>18.63 | 0.047 µs<br>0.143 µs<br>0.047 µs<br>0.186 µs | 5.81 ms*<br>**0.42 ms**<br>0.95 ms*<br>0.00 ms | 0.08 ms (Scrambled)<br>CANNOT<br>**0.04 ms**<br>CANNOT |
| **$N = 10,000$** | HashMap<br>TreeMap<br>LinkedHashMap<br>Sorted Array | 1.84<br>4.88<br>1.62<br>— | 8.26<br>27.82<br>7.15<br>31.63 | 0.083 µs<br>0.278 µs<br>0.071 µs<br>0.316 µs | 6.80 ms*<br>**1.03 ms**<br>4.36 ms*<br>0.00 ms | 0.66 ms (Scrambled)<br>CANNOT<br>**0.26 ms**<br>CANNOT |
| **$N = 100,000$** | HashMap<br>TreeMap<br>LinkedHashMap<br>Sorted Array | 13.97<br>50.18<br>7.91<br>— | **6.35**<br>61.78<br>**8.99**<br>48.29 | **0.064 µs**<br>0.618 µs<br>**0.090 µs**<br>0.483 µs | 54.16 ms*<br>**18.43 ms**<br>42.13 ms*<br>0.00 ms | 2.72 ms (Scrambled)<br>CANNOT<br>**0.75 ms**<br>CANNOT |

*(\* indicates extra $O(N \log N)$ sorting was required)*

---

## 4. Scalability Inquiry & Deployment Decision

### 4.1 Acceptable Response Time SLA
For interactive pharmacy dispensing at POS terminals, human perceptual immediacy requires response latency **under 10 ms**. Even for high-frequency barcode scanner APIs or automated robotic dispensing cabinets, an SLA of **$< 1.0$ millisecond (1000 µs)** is standard.
- Our measured lookup latencies:
  - `HashMap`: **0.064 µs** (15,000x faster than SLA limit!)
  - `LinkedHashMap`: **0.090 µs** (11,000x faster than SLA limit!)
  - `TreeMap`: **0.618 µs** (1,600x faster than SLA limit!)

### 4.2 Architectural Deployment Recommendation: `LinkedHashMap`
**We recommend deploying `LinkedHashMap` as the primary inventory store.**
- **Defense:**
  1. Pharmacists execute lookups continuously all day long. `LinkedHashMap` provides virtually identical $O(1)$ query speed to `HashMap` (8.99 ms vs 6.35 ms for 100k lookups).
  2. Report 2 (chronological addition) is natively supported in **0.75 ms** with zero extra sorting or auxiliary metadata structures.
  3. Report 1 (alphabetical code order) is executed only **once per evening**. Performing a single 42 ms sort on 100,000 elements at 11:59 PM is completely negligible and undetectable to hospital operations.
  4. If `TreeMap` were used instead, daytime queries would suffer a ~10x performance penalty on every single transaction, and Report 2 could not be generated without custom timestamps.

### 4.3 Why `Hashtable` Is Not Preferred in New Code
1. **Coarse-Grained Synchronization Bottleneck:** Every method in `Hashtable` (`get`, `put`, `remove`, `size`) is guarded by `synchronized`. In a multi-pharmacist or multi-threaded hospital server, all threads block each other on a single monitor, destroying throughput.
2. **Modern Replacement:**
   - For single-threaded or thread-confined use: `HashMap` / `LinkedHashMap` (zero synchronization overhead).
   - For concurrent access: `ConcurrentHashMap` (lock stripping / CAS bucket locking allowing hundreds of concurrent readers and writers).
3. **Legacy Restrictions:** `Hashtable` forbids `null` keys and values, whereas `HashMap` permits one `null` key and arbitrary `null` values.
4. `Hashtable` uses outdated `Enumeration` rather than standard fail-fast `Iterator`.

---

## 5. Alternative Concept: Sorted Array with `Arrays.binarySearch()`

### 5.1 Complexity Comparison Table

| Metric | Sorted Array (`Arrays.binarySearch`) | `HashMap<String, Medicine>` | `TreeMap<String, Medicine>` |
| :--- | :---: | :---: | :---: |
| **Lookup Time** | $O(\log N)$ | $O(1)$ | $O(\log N)$ |
| **Insertion Time** | **$O(N)$** (shifting array elements) | Amortized $O(1)$ | $O(\log N)$ |
| **Report 1 (Code Order)** | **$O(1)$** (already sorted in place) | $O(N \log N)$ (must sort) | $O(N)$ (in-order tree traversal) |
| **Report 2 (Insertion Order)**| **Impossible** (original indices lost) | Requires secondary queue | Requires secondary queue |
| **Memory Overhead** | **Zero pointer overhead** (contiguous array of object references) | High (Bucket table + `Node` objects + hash fields) | High (`Entry` objects with `left`, `right`, `parent`, `color`) |

### 5.2 Evaluation
While the sorted array exhibits the lowest memory footprint and instant code-order traversal, its **$O(N)$ insertion cost** is fatal for an active inventory: every time a pharmacist adds a new medicine or receives an emergency supplier shipment, thousands of elements must be shifted in memory.
