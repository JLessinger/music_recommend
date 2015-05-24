package spotify_hack.kdtree;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

/**
 * KDNode - represents a divide in some dimension, holds an E
 *
 * @param <E> a comparable in multiple dimensions, e.g. a star
 * @param <T> is a comparable, many of which are contained in E
 */
public class KDNode<E extends MultiComparable<T>, T extends Comparable<T>> extends KDTree<E, T> {

    //how many random elements to select from the list to guess the median
    //of the entire list
    private final int SAMPLE = 100;


    private KDTree<E, T> left;
    private KDTree<E, T> right;
    private E value;


    //For testing
    KDNode() {
    }


    //Inputs: The number of dimensions, d
    //        The non-empty List of Multicomparables data

    /**
     * @param d    the number of dimensions the node resides in
     * @param data the list of E's to store
     */
    public KDNode(int d, List<E> data) throws IllegalArgumentException {
        this(d, data, 0);
    }

    //Inputs: The number of dimensions, d
    //        The non-empty List of Multicomparables data
    //        The node's depth
    private KDNode(int dim, List<E> data, int dep) throws IllegalArgumentException {

        dimension = dim;
        depth = dep;

        //read in routine
        int index = depth % dimension;
        E med = approxMedian(data, index);
        value = med;

        if (med.getDimension() != dimension) {
            throw new IllegalArgumentException("Dimension of " + med + " does not match dimension " + dimension);
        }

        List<E> leftList = selectSmaller(data, index, med);
        List<E> rightList = selectBigger(data, index, med);
        //rightList contains elements >=, including median. remove it.
        rightList.remove(med);
        if (leftList.size() > 0) {

            left = new KDNode<E, T>(dim, leftList, depth + 1);
        } else {
            left = new KDEmpty<E, T>(dim, depth + 1);
        }
        if (rightList.size() > 0) {
            //	    System.out.println("making right subtree");
            right = new KDNode<E, T>(dim, rightList, depth + 1);
        } else {
            right = new KDEmpty<E, T>(dim, depth + 1);
        }
    }


    //Inputs: Two non-null lists sorted from smallest to greatest by
    //           distance to ref
    //        The maximum size (if bounded)
    //        Whether to bound the list
    //Output: The two lists merged (still sorted) and truncated after n elements
    List<E> merge(List<E> l1, List<E> l2, E ref, int n, boolean bounded) {

        List<E> merged = new ArrayList<E>();
        int i = 0;
        int j = 0;
        while (i < l1.size() && j < l2.size()) {
            if (l1.get(i).getDistance(ref).compareTo(l2.get(j).getDistance(ref)) < 0) {
                merged.add(l1.get(i));
                if (bounded && merged.size() > n) {
                    merged.remove(merged.size() - 1);
                }
                i++;
            } else {
                merged.add(l2.get(j));
                if (bounded && merged.size() > n) {
                    merged.remove(merged.size() - 1);
                }
                j++;
            }
        }
        while (i < l1.size()) {
            merged.add(l1.get(i));
            if (bounded && merged.size() > n) {
                merged.remove(merged.size() - 1);
            }
            i++;
        }
        while (j < l2.size()) {
            merged.add(l2.get(j));
            if (bounded && merged.size() > n) {
                merged.remove(merged.size() - 1);
            }
            j++;
        }
        //	System.out.println("merged" + merged);
        return merged;
    }

    //Inputs: An E to insert
    //        A non-null list<E> sorted from smallest to greatest by
    //            distance to ref
    //        The E to which to compare distance to
    //        The maximum size (if bounded)
    //        Whether to bound list
    //Output: list still sorted, with newE added
    List<E> insert(E newE, List<E> list, E ref, int n, boolean bounded) {

        int i = 0;
        while (i < list.size() && newE.getDistance(ref).compareTo(list.get(i).getDistance(ref)) > 0) {
            i++;
        }
        if (i == list.size()) {
            list.add(newE);
        } else {
            list.add(i, newE);
        }
        if (bounded && list.size() > n) {
            list.remove(list.size() - 1);
        }
        //		System.out.println("inserted" + list);
        return list;
    }


    //Inputs: the number of neighbors to find
    //        an E whose neighbors to find
    //Output: a list containing the n E's closest to server
    //in order of increasing distance including server itself

    /**
     * @param n      the number of neighbors to find
     * @param search the E to server near for neighbors
     * @return the proper number of neighbors sorted by increasing distance, including server itself
     */
    public List<E> getClosestN(int n, E search) {
        if(search==null) {
            throw new IllegalArgumentException("cannot search for null item");
        }
        if (getLeft().isEmpty() && getRight().isEmpty()) {
            List<E> subtree = new ArrayList<>();
            // to return no more than n guesses
            //n=0 and n>0 have separate base cases
            if (n == 0) {
                return subtree;
            } else {
                subtree.add(value);
            }
            return subtree;
        }
        int dim = depth % dimension;
        KDTree<E, T> searchFirst, searchSecond;
        if (search.compareTo(value, dim) > 0) {
            searchFirst = getRight();
            searchSecond = getLeft();
        } else {
            searchFirst = getLeft();
            searchSecond = getRight();
        }
        List<E> guesses = null;
        if (searchFirst.isEmpty()) {
            guesses = new ArrayList<>();
        } else {
            guesses = searchFirst.getClosestN(n, search);
        }
        boolean putValue = guesses.size() < n || (guesses.size() > 0 && guesses.get(guesses.size() - 1).getDistance(search).compareTo(value.getDistance(search)) > 0);
        if (putValue) {
            guesses = insert(value, guesses, search, n, true);
        }
        //guesses now contains at least one E
        //double check this boolean. possible mistake
        boolean traverseSecond = guesses.size() > 0 && (guesses.size() < n || guesses.get(guesses.size() - 1).getDistance(search).compareTo(value.subtract(search, dim)) >= 0);

        if (!searchSecond.isEmpty() && traverseSecond) {
            List<E> moreGuesses = searchSecond.getClosestN(n, search);
            guesses = merge(guesses, moreGuesses, search, n, true);
        }
        //	System.out.println("guesses:" + guesses );
        return guesses;
    }

    //Inputs: the radius within which to server for E's
    //Output: a list containing all the E's whose distance from server
    //    is <= r, including E itself

    /**
     * @param r      the radius within which to server for neighbors
     * @param search the E to serach near for neighbors
     * @return all the neighbors found within the radius, including server itself
     */
    public List<E> getClosestR(T r, E search) {

        if(search==null) {
            throw new IllegalArgumentException("cannot search for null item");
        }

        if (getLeft().isEmpty() && getRight().isEmpty()) {
            List<E> subtree = new ArrayList<>();
            if (value.getDistance(search).compareTo(r) <= 0) {
                subtree.add(value);
            }
            return subtree;
        }
        int dim = depth % dimension;
        KDTree<E, T> searchFirst, searchSecond;
        if (search.compareTo(value, dim) > 0) {
            searchFirst = getRight();
            searchSecond = getLeft();
        } else {
            searchFirst = getLeft();
            searchSecond = getRight();
        }
        List<E> neighbors = null;
        if (searchFirst.isEmpty()) {
            neighbors = new ArrayList<>();
        } else {
            neighbors = searchFirst.getClosestR(r, search);
        }
        if (value.getDistance(search).compareTo(r) <= 0) {
            neighbors = insert(value, neighbors, search, -1, false);
        }
        //guesses now contains at least one E
        boolean traverseSecond = !searchSecond.isEmpty() && value.subtract(search, dim).compareTo(r) <= 0;
        if (traverseSecond) {
            List<E> moreNeighbors = searchSecond.getClosestR(r, search);
            neighbors = merge(neighbors, moreNeighbors, search, -1, false);
        }
        return neighbors;
    }


    public KDTree<E, T> getLeft() {
        return left;
    }

    public KDTree<E, T> getRight() {
        return right;
    }

    public String toString() {

        String s = "\n";
        for (int i = 0; i < getDepth(); i++) {
            s += " ";
        }
        s += value;
        s += getLeft().toString() + getRight().toString();
        return s;
    }


    //Inputs: Indices i and j
    //        A List<E>
    void swap(int i, int j, List<E> data) {
        //	System.out.println("Swapping " + i + " " + j + " in " + data);
        E k = data.get(i);
        data.set(i, data.get(j));
        data.set(j, k);
    }

    //Simple selection sort
    //Differs from Collections.sort - uses modified compareTo()

    //Inputs: A List of MultiComparables
    //        The index of the dimension by which to compare
    //Output: The list sorted by that dimension
    List<E> sort(List<E> data, int index) {

        for (int i = 0; i < data.size() - 1; i++) {
            int min = i;
            for (int j = i + 1; j < data.size(); j++) {
                if (data.get(j).compareTo(data.get(min), index) < 0) {
                    min = j;
                }
            }
            swap(i, min, data);
        }
        return data;
    }

    //Inputs: A List<E> data
    //        The index of the dimension by which to find the median
    //Output: A member of data close to the median
    E approxMedian(List<E> data, int index) {

        int size = data.size();
        if (size < SAMPLE) {
            return sort(data, index).get((size - 1) / 2);
        } else {
            //	    System.out.println("size >= sample");
            List<Integer> indices = new ArrayList<>();
            for (int i = 0; i < SAMPLE; i++) {
                int r = (int) (Math.random() * size);
                while (indices.contains(r)) {
                    r++;
                }
                indices.add(r);
            }

            List<E> sample = new ArrayList<E>();
            for (Integer i : indices) {
                if (i < data.size()) {
                    sample.add(data.get(i));
                }
            }
            return sort(sample, index).get((sample.size() - 1) / 2);
        }
    }

    //Inputs: A List
    //        The index of the dimension of comparison
    //        The member of the list to compare to
    //Output: A sublist containing only the elements smaller  than med
    List<E> selectSmaller(List<E> data, int index, E med) {

        List<E> smaller = new ArrayList<E>();
        for (E e : data) {
            if (e.compareTo(med, index) < 0) {
                smaller.add(e);
            }
        }
        return smaller;
    }

    //Inputs: A List
    //        The index of the dimension of comparison
    //        The member of the list to compare to
    //Output: A sublist containing only the elements bigger than med
    List<E> selectBigger(List<E> data, int index, E med) {

        List<E> bigger = new ArrayList<E>();
        for (E e : data) {
            if (e.compareTo(med, index) >= 0) {
                bigger.add(e);
            }
        }
        return bigger;
    }

    public boolean isEmpty() {
        return false;
    }

    public int getSize() {
        return 1 + left.getSize() + right.getSize();
    }

    public E getValue() {
        return value;
    }

    /**
     * @param dimIndex the index of the dimension in which to compare
     */
    public int compareTo(KDNode<E, T> other, int dimIndex) {

        //check not necessary because if other is of same type, it must have same dimension (unless two instances of the same type of multicomparable return different dimensions, which shouldnt' happen
    /*	if(other.getDimension()!=dimension){
        throw new IllegalArgumentException("Different dimension");
		}*/
        if (dimIndex < 0 || dimIndex >= dimension) {
            throw new IllegalArgumentException("Dimension index negative or too large.");
        } else {
            ImmutableList<T> a1 = value.getCoordinates();
            ImmutableList<T> a2 = other.getValue().getCoordinates();
            T c1 = a1.get(dimIndex);
            T c2 = a2.get(dimIndex);
            return c1.compareTo(c2);
        }
    }
}