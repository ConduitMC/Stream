package systems.conduit.stream;

public interface Callback<T> {
    void callback(T response);
}
