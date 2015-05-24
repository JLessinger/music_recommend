package spotify_hack.kdtree;

import com.google.common.collect.ImmutableList;

public interface MultiComparable<T extends Comparable<T>> {

    public String getName();

    public String getID();

    public ImmutableList<T> getCoordinates();

    public int getDimension();

    public int compareTo(MultiComparable<T> m, int ind);

    public T getDistance(MultiComparable<T> m);

    //returns absolute value for purpose of comparing distances
    //not that this distance is distinct from getDistance()
    //because it only computes in one dimension.
    public T subtract(MultiComparable<T> m, int dim);
}