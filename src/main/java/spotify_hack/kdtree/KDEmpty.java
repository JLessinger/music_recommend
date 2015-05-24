package spotify_hack.kdtree;

import java.util.ArrayList;
import java.util.List;

/**
 * Placeholder for a null. Referenced only by leaves
 *
 * @param<E> a datum comparable in multiple dimensions, e.g. a star
 * @param<T> a comparable, many of which are contained in E
 */
class KDEmpty<E extends MultiComparable<T>, T extends Comparable<T>> extends KDTree<E, T> {

    public KDEmpty(int dim, int dep) {
        dimension = dim;
        depth = dep;
    }

    //never actually called
    public List<E> getClosestN(int n, E search) {
        return new ArrayList<E>();
    }

    //never actually called
    public List<E> getClosestR(T r, E search) {
        return new ArrayList<E>();
    }

    public boolean isEmpty() {
        return true;
    }

    public String toString() {

        String s = "\n";
        for (int i = 0; i < depth; i++) {
            s += " ";
        }
        s += "{}";
        return s;
    }

    public KDTree<E, T> getLeft() {
        return null;
    }

    public KDTree<E, T> getRight() {
        return null;
    }


    public int getSize() {
        return 0;
    }

    public E getValue() throws NullPointerException {
        throw new NullPointerException("Empty node.");
    }
}