import org.apache.bookkeeper.util.ProvaJacoco;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestProva {
    @Test
    void demoTestMethod() {
        assertTrue(true);
    }
    @Test
    public void whenEmptyString_thenAccept() {
        ProvaJacoco palindromeTester = new ProvaJacoco();
        assertTrue(palindromeTester.isPalindrome(""));
    }
}
