package spotify_hack.kdtree;
import com.google.common.collect.ImmutableList;

/**
 * FOR TESTING ONLY
 * A MultiComparable in n-space.
 */
public class Star implements MultiComparable<Double> {

    private final int dimension;
    private final String name;
    private final int id;

    private final ImmutableList<Double> coords;

    /**
     * @param dim the dimension in which to find the difference
     * @throws IllegalArgumentException if the other MultiComparable is of a different type or resides in a different number of dimensions
     * @returns the absolute value of the difference
     */
    public Double subtract(MultiComparable<Double> other, int dim) throws IllegalArgumentException {

        if (other instanceof Star && other.getDimension() == getDimension() && dim < dimension) {
            return Math.abs(getCoordinates().get(dim) - other.getCoordinates().get(dim));
        } else {
            throw new IllegalArgumentException("First argument of wrong type or dimension, or second argument too large.");
        }
    }

    /**
     * @throws IllegalArgumentException if the other object resides in a different number of dimensions
     * @returns the distance in n-space squared
     */
    public Double getDistance(MultiComparable<Double> other) throws IllegalArgumentException {

        if (other instanceof Star && other.getDimension() == getDimension()) {

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

    /**
     * @param ind the dimensions in which to compare the two MultiComparables
     */
    public int compareTo(MultiComparable<Double> m, int ind) {
        return coords.get(ind).compareTo(m.getCoordinates().get(ind));
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(coords);
    }

    @Override
    public boolean equals(Object o) {

        return (o != null) &&
                (o instanceof Star) &&
                coords.equals(((Star) o).getCoordinates());
    }


    /**
     * @param dim the number of dimensions in which the star resides
     * @param n   name
     * @param i   id
     * @param co  coordinates
     * @throws NullPointerException if the array is null or holds null
     */
    public Star(int dim, String n, int i, Double[] co) throws NullPointerException {

        if (co == null) {
            throw new NullPointerException("Coordinate array null.");
        }
        ImmutableList.Builder<Double> ilbd = new ImmutableList.Builder<>();
        for (int k = 0; k < co.length; k++) {
            if (co[k] == null) {
                throw new NullPointerException("Coordinate " + k + " is null.");
            } else {
                ilbd.add(co[k]);
            }
        }
        dimension = dim;
        name = n;
        id = i;
        coords = ilbd.build();
    }

    /**
     * @param dim the number of dimensions in which the star resides
     * @param n   name
     * @param i   id
     * @param ild the coordinates
     * @throws NullPointerException if coordinate list is null
     */
    public Star(int dim, String n, int i, ImmutableList<Double> ild) throws NullPointerException {

        if (ild == null) {
            throw new NullPointerException("Coordinate list null.");
        }
        if (ild.size() != dim) {
            throw new IllegalArgumentException("Dimension does not match number of coordinates.");
        }
        dimension = dim;
        name = n;
        id = i;
        coords = ild;
    }


    @Override
    public String toString() {
        String s = "Name: " + name + " ID: " + id + " dim: " + dimension + " co: ";
        for (int i = 0; i < dimension; i++) {
            s += coords.get(i).intValue() + " ";
        }
        return s;
    }

    /**
     * @returns the number of dimensions in which the star resides
     */
    public int getDimension() {
        return dimension;
    }

    public String getName() {
        return name;
    }

    //THIS CHANGED WITH REDEFINITION OF MULTICOMPARABLE
    public String getID() {
        return "" + id;
    }

    public ImmutableList<Double> getCoordinates() {
        return coords;
    }

    public Star copy() {
        return new Star(dimension, name, id, ImmutableList.copyOf(coords));
    }
}