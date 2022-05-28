package ExtensionSPIImpl;

public class Holder<S> {
    private volatile S value;

    public S getValue() {
        return value;
    }

    public void setValue(S value) {
        this.value = value;
    }
}
