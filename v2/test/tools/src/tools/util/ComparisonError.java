package tools.util;

public class ComparisonError extends junit.framework.AssertionFailedError
{
    public ComparisonError() {
        super();
    }

    public ComparisonError(String msg) {
        super(msg);
    }
}
