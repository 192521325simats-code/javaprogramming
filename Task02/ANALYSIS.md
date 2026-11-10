# Task 02: When Is ArrayList an Appropriate Engineering Choice?

**Student Register Number:** 192521325  
**Course:** CSA09 – Programming in Java (CO2)  
**Bloom's Level:** K4 – Analyse  

---

## 1. Problem Context: Music Playlist Editor

A streaming application enables users to manage music playlists. Typical operations include:
- Appending songs at the end of the playlist.
- Inserting songs at specific arbitrary indices.
- Deleting songs from arbitrary positions.
- Direct random access: jump directly to the $k$-th song (e.g., shuffle, seek, scrub, track selection).
- Sequential playback: iterating through the full playlist.

Currently, the playlist is stored in `ArrayList<Song>`. We designed empirical benchmarks comparing `ArrayList` and `LinkedList` across three playlist scales ($N = 1,000$, $10,000$, and $100,000$ songs).

---

## 2. Experimental Data Summary

| Size ($N$) | Operation | ArrayList (ms) | LinkedList (ms) | Speedup / Winner |
| :--- | :--- | :---: | :---: | :---: |
| **$N = 1,000$** | Add at End | 3.30 | 1.21 | LinkedList |
| | Get $k$-th (Random) | **0.14** | 1.07 | **ArrayList (7.6x faster)** |
| | Sequential Iteration | **0.21** | 0.35 | **ArrayList** |
| | Insert at Front | 1.00 | **0.47** | **LinkedList (2.1x faster)** |
| | Insert in Middle | **0.37** | 2.21 | **ArrayList (6.0x faster)** |
| | Remove at Front | 0.59 | **0.17** | **LinkedList (3.5x faster)** |
| **$N = 10,000$** | Add at End | **1.52** | 4.35 | **ArrayList (2.9x faster)** |
| | Get $k$-th (Random) | **0.50** | 30.60 | **ArrayList (61.2x faster)** |
| | Sequential Iteration | 0.69 | 0.65 | Comparable |
| | Insert at Front | 5.62 | **0.49** | **LinkedList (11.5x faster)** |
| | Insert in Middle | **3.18** | 44.35 | **ArrayList (13.9x faster)** |
| | Remove at Front | 6.53 | **0.11** | **LinkedList (59.4x faster)** |
| **$N = 100,000$** | Add at End | **21.93** | 27.98 | **ArrayList (1.3x faster)** |
| | Get $k$-th (Random) | **0.46** | 644.61 | **ArrayList (1,401x faster)** |
| | Sequential Iteration | 1.45 | 1.15 | Comparable |
| | Insert at Front | 53.33 | **0.23** | **LinkedList (231.9x faster)** |
| | Insert in Middle | **27.74** | 503.53 | **ArrayList (18.1x faster)** |
| | Remove at Front | 78.88 | **0.11** | **LinkedList (717x faster)** |

---

## 3. Engineering Operating Boundary: When to Recommend ArrayList

### 3.1 When ArrayList is the Superior Engineering Choice:
1. **Random Access Dominated:** Any application requiring frequent indexed lookups (`get(k)`, binary search, shuffle). `ArrayList` provides true $O(1)$ constant time access via direct pointer arithmetic on contiguous memory:
   $$\text{Address}(k) = \text{Base} + k \times \text{reference\_size}$$
2. **End-Appending & Sequential Traversal:** For building standard queues/lists where items are appended to the tail, `ArrayList` exhibits amortized $O(1)$ time and superior L1/L2 CPU cache line locality.
3. **Insertions/Deletions in the Middle:** Counter-intuitively, `ArrayList` outperforms `LinkedList` for middle insertions when $N$ is moderate to large (e.g., at $N = 100,000$, ArrayList took 27.74 ms vs LinkedList's 503.53 ms). While LinkedList splices references in $O(1)$, it must first traverse $N/2$ node pointers ($O(N)$ pointer chasing with severe cache misses). In contrast, `ArrayList` shifts elements via hardware-accelerated `System.arraycopy()`.

### 3.2 When to Stop Recommending ArrayList:
- **High-Frequency Front / Head Operations:** If the workload frequently inserts or deletes at or near index 0 (`add(0, item)`, `remove(0)`), `ArrayList` degrades to $O(N)$ time per operation, copying the entire tail of the array on every call.
- **Unbounded Memory Fragmentation:** If large capacity jumps (1.5x resize) cause memory allocation spikes or if reallocation copies cannot be tolerated in latency-critical real-time threads.

---

## 4. Changed Condition: The "Up Next" Queue & ArrayDeque

### 4.1 Re-evaluation
The app introduces an "Up Next" queue where songs are frequently appended/prepended at both ends (`addFirst`, `addLast`) and dequeued from the head (`removeFirst`).

Under this access pattern:
- **`ArrayList` is completely inappropriate:** Inserting at index 0 forces $O(N)$ element copying on every track queued. In our test of 50,000 operations, `ArrayList` took 25.57 ms (even throttled).
- **`LinkedList`** achieves $O(1)$ double-ended insertion/removal, but creates a separate `Node` object per element, causing GC churn and heap fragmentation.
- **`ArrayDeque` is the optimal engineering choice:** It implements a resizable circular array buffer with `head` and `tail` pointers.
  - Adding/removing at head or tail takes strict $O(1)$ amortized time.
  - No element shifting is required; indices simply wrap around via bitmasking: `head = (head - 1) & (elements.length - 1)`.
  - Zero wrapper node objects are allocated.

---

## 5. Alternative Concept & Complexity Analysis

### 5.1 Complexity and Per-Element Memory Footprint

| Operation | `ArrayList<E>` | `LinkedList<E>` | `ArrayDeque<E>` | Plain Array with Manual Shifting |
| :--- | :---: | :---: | :---: | :---: |
| **Add at End** | Amortized $O(1)$ | $O(1)$ | Amortized $O(1)$ | $O(1)$ (if capacity remains), else $O(N)$ |
| **Insert at Front** | $O(N)$ | $O(1)$ | Amortized $O(1)$ | $O(N)$ (requires manual loop shift) |
| **Insert in Middle** | $O(N)$ | $O(N)$ traverse + $O(1)$ splice | $O(N)$ | $O(N)$ |
| **Get $k$-th Element**| $O(1)$ | $O(N)$ ($N/2$ hops average) | $O(1)$ (if indexed) | $O(1)$ |
| **Sequential Iterate**| $O(N)$ (highest cache hits) | $O(N)$ (cache misses) | $O(N)$ | $O(N)$ |
| **Memory per Element**| **4 to 8 bytes** (single pointer in array slot) | **24 to 32 bytes** (12-byte header + 3 references: `item`, `next`, `prev`) | **4 to 8 bytes** (pointer slot in circular array) | **4 to 8 bytes** |

### 5.2 Plain Array with Manual Shifting
If the playlist were implemented using a primitive `Song[] array` with manual shifting:
```java
// Insert at front
Song[] newArr = (size == capacity) ? new Song[capacity * 2] : array;
for (int i = size; i > 0; i--) {
    newArr[i] = array[i - 1];
}
newArr[0] = newSong;
```
- **Time Complexity:** Identical to `ArrayList` ($O(N)$ for shifts, $O(1)$ for random access).
- **Comparison:** `ArrayList` internally delegates to the native intrinsic `System.arraycopy()`, which uses SIMD/vectorized assembly instructions (e.g., `memmove`/`rep movsb`), making it 3–5x faster than a manual Java `for` loop. Thus, a raw array offers no algorithmic advantage over `ArrayList`, while adding high boilerplate and error-prone index management.
