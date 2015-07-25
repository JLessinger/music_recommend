package recommender;

import com.google.common.collect.ImmutableList;
import kdtree.MultiComparable;

/**
 * Created by jonathan on 5/23/15.
 */
public class Song implements MultiComparable<Double> {

    private final String id;
    private final String name;
    private final int dimension;
    private final ImmutableList<Double> coords;


    /**
     * @param dim the number of dimensions in which the song resides
     * @param n   name
     * @param id   id
     * @param ild the coordinates
     * @throws NullPointerException if coordinate list is null
     */
    public Song(int dim, String n, String id, ImmutableList<Double> ild) throws NullPointerException {

        if (ild == null) {
            throw new NullPointerException("Coordinate list null.");
        }
        if (ild.size() != dim) {
            throw new IllegalArgumentException("Dimension does not match number of coordinates.");
        }
        dimension = dim;
        name = n;
        this.id = id;
        coords = ild;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public ImmutableList<Double> getCoordinates() {
        return coords;
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    @Override
    public int compareTo(MultiComparable<Double> m, int ind) {
        return coords.get(ind).compareTo(m.getCoordinates().get(ind));
    }

    @Override
    public Double getDistance(MultiComparable<Double> other) {
        if (other instanceof Song && other.getDimension() == getDimension()) {

            Double sumsq = 0.0;
            for (int i = 0; i < dimension; i++) {
                Double diff = coords.get(i) - other.getCoordinates().get(i);
                sumsq += Math.pow(diff, 2);
            }
            return sumsq;
        } else {
            throw new IllegalArgumentException("Cannot compare, " + other + " is of a different type");
        }
    }

    @Override
    public Double subtract(MultiComparable<Double> other, int dim) {

        if (other instanceof Song && other.getDimension() == getDimension() && dim < dimension) {
            return Math.abs(getCoordinates().get(dim) - other.getCoordinates().get(dim));
        } else {
            throw new IllegalArgumentException("First argument of wrong type or dimension, or second argument too large.");
        }
    }

    @Override
    public String toString() {
        String s = "Name: " + name + " ID: " + id + " dim: " + dimension + " co: ";
        for (int i = 0; i < dimension; i++) {
            s += coords.get(i).intValue() + " ";
        }
        return s;
    }
}
