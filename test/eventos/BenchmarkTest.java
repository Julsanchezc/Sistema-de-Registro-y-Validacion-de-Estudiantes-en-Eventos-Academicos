package eventos;

import eventos.model.Student;
import eventos.structures.AvlTree;
import eventos.structures.BstTree;
import eventos.structures.HashTable;
import eventos.structures.MinHeap;
import org.junit.jupiter.api.*;

import java.util.Comparator;
import java.util.Random;

/**
 * Isolated structure benchmarks.
 *
 * Each operation is measured on its own FRESH data structure.
 * For delete: the structure is pre-populated OUTSIDE the timed block,
 * so the clock starts with a full set of n — no cascading shrinkage.
 *
 * Run with:  ./gradlew test
 * Results are printed to stdout (not fail/pass criteria).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Data Structure Benchmarks")
class BenchmarkTest {

    private static final int[]  SIZES    = { 10_000, 100_000, 1_000_000 };
    private static final int    WARMUP_N = 1_000;
    private static final String SEP      = "-".repeat(66);
    private static final String DOUBLE   = "=".repeat(66);

    // ── Lifecycle ──────────────────────────────────────────────────

    @BeforeAll
    static void header() {
        System.out.println("\n" + DOUBLE);
        System.out.println("  BENCHMARK SUITE — Data Structures (JUnit 5)");
        System.out.printf ("  Dataset : Fisher-Yates shuffle, seed=42, fresh per test%n");
        System.out.printf ("  Sizes   : %,d  /  %,d  /  %,d%n", SIZES[0], SIZES[1], SIZES[2]);
        System.out.println("  Delete  : each size starts from a FULL independent tree.");
        System.out.println(DOUBLE);
    }

    @AfterAll
    static void footer() {
        System.out.println("\n" + DOUBLE);
        System.out.println("  All benchmarks complete.  Times = wall-clock ms.");
        System.out.println("  Warmup (n=1,000) executed before each group.");
        System.out.println(DOUBLE + "\n");
    }

    // ── AVL vs BST ─────────────────────────────────────────────────

    @Test @Order(1)
    @DisplayName("AVL vs BST — Insert (random order)")
    void insert_avlVsBst() {
        warmupAvl(); warmupBst();

        long[] avl = new long[SIZES.length];
        long[] bst = new long[SIZES.length];

        for (int i = 0; i < SIZES.length; i++) {
            Student[] data = shuffled(SIZES[i]);

            // AVL: measure inserting n elements into an empty tree
            AvlTree avlTree = new AvlTree();
            long t = System.nanoTime();
            for (Student s : data) avlTree.insert(s);
            avl[i] = ms(t);

            // BST: same data, independent empty tree
            BstTree bstTree = new BstTree();
            t = System.nanoTime();
            for (Student s : data) bstTree.insert(s);
            bst[i] = ms(t);
        }

        printTable("INSERT (random order)", avl, bst);
    }

    @Test @Order(2)
    @DisplayName("AVL vs BST — Find (full tree of n, n queries)")
    void find_avlVsBst() {
        warmupAvl(); warmupBst();

        long[] avl = new long[SIZES.length];
        long[] bst = new long[SIZES.length];

        for (int i = 0; i < SIZES.length; i++) {
            Student[] data = shuffled(SIZES[i]);

            // AVL: populate (not measured), then measure n finds
            AvlTree avlTree = new AvlTree();
            for (Student s : data) avlTree.insert(s);
            long t = System.nanoTime();
            for (Student s : data) avlTree.find(s.getId());
            avl[i] = ms(t);

            // BST: independent fresh population, then n finds
            BstTree bstTree = new BstTree();
            for (Student s : data) bstTree.insert(s);
            t = System.nanoTime();
            for (Student s : data) bstTree.contains(s.getId());
            bst[i] = ms(t);
        }

        printTable("FIND (n queries in full tree)", avl, bst);
    }

    @Test @Order(3)
    @DisplayName("AVL vs BST — Delete (fresh full set of n per size)")
    void delete_avlVsBst() {
        warmupAvl(); warmupBst();

        /*
         * Methodology: for every n, a FRESH tree is populated before the clock starts.
         * The timed block deletes all n elements from a tree that always has n items at t=0.
         * This prevents the "delete 100, then 99, then 98..." cascade problem.
         */
        long[] avl = new long[SIZES.length];
        long[] bst = new long[SIZES.length];

        for (int i = 0; i < SIZES.length; i++) {
            Student[] data = shuffled(SIZES[i]);

            // AVL: populate FRESH tree (not measured) → delete all n
            AvlTree avlTree = new AvlTree();
            for (Student s : data) avlTree.insert(s);
            long t = System.nanoTime();
            for (Student s : data) avlTree.remove(s.getId());
            avl[i] = ms(t);
            Assertions.assertEquals(0, avlTree.getStudentCount(),
                "AVL must be empty after deleting all n=" + SIZES[i]);

            // BST: independent FRESH tree (not measured) → delete all n
            BstTree bstTree = new BstTree();
            for (Student s : data) bstTree.insert(s);
            t = System.nanoTime();
            for (Student s : data) bstTree.remove(s.getId());
            bst[i] = ms(t);
            Assertions.assertEquals(0, bstTree.size(),
                "BST must be empty after deleting all n=" + SIZES[i]);
        }

        printTable("DELETE (each from independent full tree)", avl, bst);
    }

    @Test @Order(4)
    @DisplayName("AVL vs BST — Tree heights after random insert")
    void heights_avlVsBst() {
        System.out.println("\n--- HEIGHT after inserting n random elements ---");
        System.out.printf("  %-14s %12s %12s %12s%n", "", "n=10,000", "n=100,000", "n=1,000,000");
        System.out.println("  " + SEP);

        int[] avlH = new int[SIZES.length];
        int[] bstH = new int[SIZES.length];

        for (int i = 0; i < SIZES.length; i++) {
            Student[] data = shuffled(SIZES[i]);
            AvlTree   avl  = new AvlTree();
            BstTree   bst  = new BstTree();
            for (Student s : data) { avl.insert(s); bst.insert(s); }
            avlH[i] = avl.getHeight();
            bstH[i] = bst.getHeight();
        }

        System.out.printf("  %-14s %12d %12d %12d%n", "AVL height", avlH[0], avlH[1], avlH[2]);
        System.out.printf("  %-14s %12d %12d %12d%n", "BST height", bstH[0], bstH[1], bstH[2]);
        for (int i = 0; i < SIZES.length; i++) {
            double log2n   = Math.log(SIZES[i]) / Math.log(2);
            double maxAvl  = 1.44 * log2n;
            System.out.printf("  n=%,d  log2(n)=%.1f  maxAVL<=%.1f  AVL=%d  BST=%d%n",
                SIZES[i], log2n, maxAvl, avlH[i], bstH[i]);
            Assertions.assertTrue(avlH[i] <= (int) maxAvl + 2,
                "AVL height must respect O(log n) bound");
        }
    }

    // ── Degenerate case ────────────────────────────────────────────

    @Test @Order(5)
    @DisplayName("BST degenerate — sequential insert 1..2000")
    void degenerate_sequential() {
        /*
         * Sequential IDs (1, 2, 3, ...) are the worst case for BST:
         * every new node goes to the right, producing a right-skewed linked list.
         * AVL rebalances and maintains O(log n).
         *
         * n=2000 chosen deliberately: safe for BstTree's recursive find/remove
         * (stack depth = 2000, well within JVM default ~10,000 frames).
         */
        int n = 2_000;
        System.out.printf("%n--- DEGENERATE: sequential insert 1..%,d ---%n", n);

        AvlTree avl = new AvlTree();
        BstTree bst = new BstTree();
        for (int i = 1; i <= n; i++) {
            Student s = new Student(i, "s" + i, "s" + i + "@t.co", "P");
            avl.insert(s);
            bst.insert(s);
        }

        double log2n  = Math.log(n) / Math.log(2);
        double maxAvl = 1.44 * log2n;
        System.out.printf("  log2(%,d) = %.2f   max-AVL = %.2f%n", n, log2n, maxAvl);
        System.out.printf("  AVL height : %4d   (guaranteed O(log n))%n", avl.getHeight());
        System.out.printf("  BST height : %4d   (degenerate — equivalent to linked list)%n",
            bst.getHeight());

        Assertions.assertTrue(avl.getHeight() <= (int) maxAvl + 2,
            "AVL must stay O(log n) even with sequential input");
        Assertions.assertEquals(n, bst.getHeight(),
            "Sequential BST must degenerate to height n (right-skewed list)");
    }

    // ── HashTable ──────────────────────────────────────────────────

    @Test @Order(6)
    @DisplayName("HashTable — put / get / remove")
    void hashTable_putGetRemove() {
        warmupHashTable();

        long[] put = new long[SIZES.length];
        long[] get = new long[SIZES.length];
        long[] rem = new long[SIZES.length];

        for (int i = 0; i < SIZES.length; i++) {
            Student[] data = shuffled(SIZES[i]);

            // put: measure inserting n entries into an empty table
            HashTable<Integer, Student> ht = new HashTable<>();
            long t = System.nanoTime();
            for (Student s : data) ht.put(s.getId(), s);
            put[i] = ms(t);

            // get: table already full from put — measure n lookups
            t = System.nanoTime();
            for (Student s : data) ht.get(s.getId());
            get[i] = ms(t);

            // remove: FRESH table, pre-populated (not measured) → delete all n
            HashTable<Integer, Student> ht2 = new HashTable<>();
            for (Student s : data) ht2.put(s.getId(), s);
            t = System.nanoTime();
            for (Student s : data) ht2.remove(s.getId());
            rem[i] = ms(t);
            Assertions.assertTrue(ht2.isEmpty(),
                "HashTable must be empty after removing all n=" + SIZES[i]);
        }

        System.out.println("\n--- HASHTABLE ---");
        System.out.printf("  %-8s %12s %12s %12s   ms%n", "", "n=10,000", "n=100,000", "n=1,000,000");
        System.out.println("  " + SEP);
        System.out.printf("  %-8s %,12d %,12d %,12d%n", "put",    put[0], put[1], put[2]);
        System.out.printf("  %-8s %,12d %,12d %,12d%n", "get",    get[0], get[1], get[2]);
        System.out.printf("  %-8s %,12d %,12d %,12d%n", "remove", rem[0], rem[1], rem[2]);
    }

    // ── MinHeap ────────────────────────────────────────────────────

    @Test @Order(7)
    @DisplayName("MinHeap — insert / poll")
    void minHeap_insertPoll() {
        warmupMinHeap();
        Comparator<Student> byCount = Comparator.comparingInt(Student::getAttendanceCount);

        long[] ins  = new long[SIZES.length];
        long[] poll = new long[SIZES.length];

        for (int i = 0; i < SIZES.length; i++) {
            Student[] data = shuffled(SIZES[i]);

            // insert: measure inserting n elements into empty heap
            MinHeap<Student> heap = new MinHeap<>(byCount);
            long t = System.nanoTime();
            for (Student s : data) heap.insert(s);
            ins[i] = ms(t);

            // poll: FRESH heap pre-populated (not measured) → drain completely
            MinHeap<Student> heap2 = new MinHeap<>(byCount);
            for (Student s : data) heap2.insert(s);
            t = System.nanoTime();
            while (!heap2.isEmpty()) heap2.poll();
            poll[i] = ms(t);
            Assertions.assertTrue(heap2.isEmpty(),
                "MinHeap must be empty after polling all n=" + SIZES[i]);
        }

        System.out.println("\n--- MINHEAP ---");
        System.out.printf("  %-8s %12s %12s %12s   ms%n", "", "n=10,000", "n=100,000", "n=1,000,000");
        System.out.println("  " + SEP);
        System.out.printf("  %-8s %,12d %,12d %,12d%n", "insert", ins[0],  ins[1],  ins[2]);
        System.out.printf("  %-8s %,12d %,12d %,12d%n", "poll",   poll[0], poll[1], poll[2]);
    }

    // ── Helpers ────────────────────────────────────────────────────

    /** Fisher-Yates shuffle with fixed seed=42. IDs are 1..n. */
    private static Student[] shuffled(int n) {
        Student[] data = new Student[n];
        for (int i = 0; i < n; i++)
            data[i] = new Student(i + 1, "s" + (i + 1), "s" + (i + 1) + "@t.co", "P");
        Random rnd = new Random(42);
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            Student tmp = data[i]; data[i] = data[j]; data[j] = tmp;
        }
        return data;
    }

    private static long ms(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }

    private static void printTable(String label, long[] avl, long[] bst) {
        System.out.println("\n--- " + label + " ---");
        System.out.printf("  %-6s %12s %12s %12s   ms%n", "", "n=10,000", "n=100,000", "n=1,000,000");
        System.out.println("  " + SEP);
        System.out.printf("  %-6s %,12d %,12d %,12d%n", "AVL", avl[0], avl[1], avl[2]);
        System.out.printf("  %-6s %,12d %,12d %,12d%n", "BST", bst[0], bst[1], bst[2]);
    }

    // Warmup runs: let the JVM JIT-compile the hot paths before measuring.
    private static void warmupAvl() {
        Student[] d = shuffled(WARMUP_N);
        AvlTree t = new AvlTree();
        for (Student s : d) t.insert(s);
        for (Student s : d) t.find(s.getId());
        for (Student s : d) t.remove(s.getId());
    }

    private static void warmupBst() {
        Student[] d = shuffled(WARMUP_N);
        BstTree t = new BstTree();
        for (Student s : d) t.insert(s);
        for (Student s : d) t.contains(s.getId());
        for (Student s : d) t.remove(s.getId());
    }

    private static void warmupHashTable() {
        Student[] d = shuffled(WARMUP_N);
        HashTable<Integer, Student> ht = new HashTable<>();
        for (Student s : d) ht.put(s.getId(), s);
        for (Student s : d) ht.get(s.getId());
        for (Student s : d) ht.remove(s.getId());
    }

    private static void warmupMinHeap() {
        Student[] d = shuffled(WARMUP_N);
        MinHeap<Student> h = new MinHeap<>(Comparator.comparingInt(Student::getAttendanceCount));
        for (Student s : d) h.insert(s);
        while (!h.isEmpty()) h.poll();
    }
}
