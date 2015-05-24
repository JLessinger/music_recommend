package spotify_hack.kdtree;

import java.util.List;

/**
 * @param <E> a comparable in multiple dimensions, e.g. a star
 * @param <T> is a comparable, many of which are contained in E
 */
public abstract class KDTree<E extends MultiComparable<T>, T extends Comparable<T>> {

    protected int depth;

    protected int dimension;

    public int getDimension() {
        return dimension;
    }

    public int getDepth() {
        return depth;
    }

    public abstract E getValue();

    //Takes a non-negative integer and an E
    //returns the n elements in the tree closest to server
    //in order from closest to farthest, including server itself
    //if it's present

    /**
     * @param n      the number of neighbors to find
     * @param search the E to server near for neighbors
     * @return the proper number of neighbors sorted by increasing distance, including server itself
     */
    public abstract List<E> getClosestN(int n, E search);

    //takes a comparable and an E
    //returns a list in order from closest to farthest
    //of all the elements e in such that e.getDistance(server) <= r
    //including server itself if it's present

    /**
     * @param r      the radius within which to server for neighbors
     * @param search the E to serach near for neighbors
     * @return all the neighbors found within the radius, including server itself
     */
    public abstract List<E> getClosestR(T r, E search);

    public abstract boolean isEmpty();

    public abstract KDTree<E, T> getLeft();

    public abstract KDTree<E, T> getRight();

    public abstract int getSize();

    public abstract String toString();
}