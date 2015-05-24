package spotify_hack.kdtree;


import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class KDTreeTester {


    private final Double[] ds = {0.0, 0.0, 0.0};
    private final Double[] a = {1.0, 1.0};
    private final Double[] b = {2.0, 3.0};
    private final Double[] c = {3.0, 1.0};
    private final Double[] d = {1.0, 4.0};


    private final Star sol = new Star(3, "sol", 0, ds);
    private final Star A = new Star(2, "A", 0, a);
    private final Star B = new Star(2, "B", 1, b);
    private final Star C = new Star(2, "C", 2, c);
    private final Star D = new Star(2, "D", 3, d);

    private final int NUMSTARS = 5;

    private Star makeStar(String name, int id, double x, double y, double z) {

        return new Star(3, name, id, makeCoords(x, y, z));
    }


    private ImmutableList<Double> makeCoords(double x, double y, double z) {

        ImmutableList.Builder<Double> ilbd = new ImmutableList.Builder<>();
        ilbd.add(x);
        ilbd.add(y);
        ilbd.add(z);
        return ilbd.build();
    }

    private double randD() {

        return 1000 * (Math.random() - 1);
    }

    //test isn't working
    /*
    @Test
	public void randomGetClosestNTest(){

	List<Star> starlist = new ArrayList<>();
	List<Epivot<Star, Double>> alist = new ArrayList<>();
	Star pivot = makeStar("pivot", 0, randD(), randD(), randD());
	
	for(int i = 1; i < NUMSTARS; i++){
	    Star s = makeStar("", i, randD(), randD(), randD());
	    alist.add(new Epivot<Star, Double>(s, pivot));
	    starlist.add(s);
	}
	
	
	KDTree<Star, Double> atree = new KDNode<>(3, starlist);
	
	int n = (int)(Math.random()*1.2*NUMSTARS);
	if(n < alist.size()){
	    alist = alist.subList(0, n);
	}
	Collections.sort(alist);

	List<Star> output = atree.getClosestN(n, pivot);

	List<Epivot<Star, Double>> outputEpivots = new ArrayList<>();
	for(Star s: output){
	    outputEpivots.add(new Epivot<Star, Double>(s, pivot));
	}
	assertEquals(alist, outputEpivots);
    }
*/

    @Test
    public void mergeTest() {

        KDNode<Star, Double> k = new KDNode<>();

        List<Star> l1 = new ArrayList<>();
        List<Star> l2 = new ArrayList<>();
        List<Star> merged = new ArrayList<>();
        l1.add(A);
        l1.add(B);
        l2.add(C);
        l2.add(D);
        merged.add(A);
        merged.add(C);
        merged.add(B);
        assertEquals(merged, k.merge(l1, l2, A, 3, true));
        merged.add(D);
        assertEquals(merged, k.merge(l1, l2, A, -1, false));

    }

    @Test
    public void insertTest() {

        List<Star> l1 = new ArrayList<>();
        KDNode<Star, Double> k = new KDNode<>();

        l1.add(B);
        l1.add(C);
        l1.add(D);

        List<Star> inserted = new ArrayList<>();
        inserted.add(A);
        inserted.add(B);
        inserted.add(C);
        assertEquals(inserted, k.insert(A, l1, A, 3, true));

        inserted.add(1, B);
        assertEquals(inserted, k.insert(B, l1, A, -1, false));
    }

    @Test
    public void getClosestNTest() {

        List<Star> list = new ArrayList<>();
        list.add(A);
        KDTree<Star, Double> at = new KDNode<>(2, list);
        assertEquals(list, at.getClosestN(1, A));
    }

    @Test
    public void approxMedTest() {

        KDNode<Star, Double> testtree = new KDNode<>();
        List<Star> data = new ArrayList<>();
        data.add(A);
        assertEquals(testtree.approxMedian(data, 0), A);

        data.add(B);
        data.add(C);
        data.add(D);
        assertEquals(testtree.approxMedian(data, 0), D);

        for (int i = 0; i < 100; i++) {
            data.add(A);
        }
        for (int i = 0; i < 500; i++) {
            data.add(B);
        }
        assertEquals(testtree.approxMedian(data, 0), B);
        Collections.shuffle(data);
        assertEquals(testtree.approxMedian(data, 0), B);
    }

    @Test
    public void kdBuildTest() {

        List<Star> aList = new ArrayList<>();

        aList.add(sol);
        KDNode<Star, Double> startree = new KDNode<Star, Double>(3, aList);
        assert (startree.getDimension() == 3);
    }

    @Test
    public void partitions() {

        KDNode<Star, Double> testtree = new KDNode<>();
        List<Star> data = new ArrayList<>();
        data.add(A);
        assertEquals(testtree.selectSmaller(data, 0, A).size(), 0);
        assertEquals(testtree.selectBigger(data, 0, A).size(), 1);
        data.add(B);
        List<Star> sm = testtree.selectSmaller(data, 0, A);
        List<Star> bi = testtree.selectBigger(data, 0, A);
        assertEquals(sm.size(), 0);
        assertEquals(bi.size(), 2);
        assertTrue(bi.contains(A));
        assertTrue(bi.contains(B));

    }

    @Test
    public void swapTest() {

        KDNode<Star, Double> testtree = new KDNode<>();
        List<Star> simple = new ArrayList<>();

        simple.add(A);
        simple.add(B);
        simple.add(C);
        simple.add(D);
        testtree.swap(0, 1, simple);
        assertTrue(simple.size() == 4);
        assertEquals(simple.get(0), B);
        assertEquals(simple.get(1), A);


    }

    @Test
    public void sortTest() {

        KDNode<Star, Double> testtree = new KDNode<>();

        List<Star> simple = new ArrayList<>();
        List<Star> simple2 = new ArrayList<>();
        assertTrue(testtree.sort(simple, 0).isEmpty());
        simple.add(sol);
        simple2.add(sol);

        assertEquals(simple2, (testtree.sort(simple, 0)));
        simple.remove(sol);

        simple.add(A);
        simple.add(B);
        simple.add(C);
        simple.add(D);

        testtree.sort(simple, 0);

        assertEquals(simple.get(0), A);
        assertEquals(simple.get(1), D);
        assertEquals(simple.get(2), B);
        assertEquals(simple.get(3), C);

        testtree.sort(simple, 1);

        assertEquals(simple.get(0), A);
        assertEquals(simple.get(1), C);
        assertEquals(simple.get(2), B);
        assertEquals(simple.get(3), D);

    }

    //sub is a subtree of a node containing val
    //returns whether sub only contains E's >= val in dimension index
    private boolean allGreaterEq(Star val, KDTree<Star, Double> sub, int index) {

        if (sub instanceof KDEmpty) {
            return true;
        }
        return sub.getValue().compareTo(val, index) >= 0 &&
                allGreaterEq(val, sub.getLeft(), index) &&
                allGreaterEq(val, sub.getRight(), index);
    }

    private boolean allLess(Star val, KDTree<Star, Double> sub, int index) {

        if (sub instanceof KDEmpty) {
            return true;
        }
        return sub.getValue().compareTo(val, index) < 0 &&
                allLess(val, sub.getLeft(), index) &&
                allLess(val, sub.getRight(), index);
    }

    private boolean kdCorrect(KDTree<Star, Double> tree) {

        if (tree instanceof KDEmpty) {
            return true;
        }
        //	System.out.println("value: "  + tree.getValue());
        //	System.out.println("dimension" + tree.getDimension());
        int index = tree.getDepth() % tree.getDimension();
        //makes sure the whole right subtree >= this in dimension index
        return allGreaterEq(tree.getValue(), tree.getRight(), index) &&
                allLess(tree.getValue(), tree.getLeft(), index) &&
                kdCorrect(tree.getRight()) && kdCorrect(tree.getLeft());
    }

    //****this test is too dependent on Stars package. Skip it here****//
       /*
    @Test
    public void buildTreeTest() {

        List<Star> one = new ArrayList<>();
        List<Star> simple = new ArrayList<>();
        List<Star> simple2 = new ArrayList<>();

        one.add(A);

        simple.add(A);
        simple.add(B);
        simple.add(C);
        simple.add(D);

        simple2.add(D);
        simple2.add(C);
        simple2.add(A);
        simple2.add(B);

        KDTree<Star, Double> onetree = new KDNode<>(2, one);
        assertTrue(kdCorrect(onetree));

        KDTree<Star, Double> atree = new KDNode<>(2, simple);
        //System.out.println(atree);
        assertTrue(kdCorrect(atree));

        KDTree<Star, Double> atree2 = new KDNode<>(2, simple2);
        //	System.out.println(atree2);
        assertTrue(kdCorrect(atree2));

        try {
            //test entire file
            BufferedReader reader = new BufferedReader(new FileReader("csv/stardata.csv"));
            HashMap<String, ImmutableList<Double>> cbn = new HashMap<>();
            HashSet<ImmutableList<Double>> coo = new HashSet<>();
            HashSet<Integer> ids = new HashSet<>();
            List<Star> starlist = StarParser.readin(reader, cbn, coo, ids);

            KDTree<Star, Double> full = new KDNode<>(3, starlist);

            assertTrue(kdCorrect(full));
        } catch (Exception e) {
            System.err.println("Entire tree test failed. File not found.");
        }
    }         */
}