package infra;

/**
 * This interface provides an extensible enum-like framework that can be used to form unions of different
 * quantities.
 * 
 * @author nrowell
 *
 * @param <T> The generic type of the enum; e.g. Filter.
 */
public interface Quantity<T extends Enum<T>> {

}
