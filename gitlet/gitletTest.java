package gitlet;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by PeterLee on 12/10/15.
 */
public class gitletTest {
    @Test
    public void testBasicLog() {
        ArrayList<String> messages = new ArrayList<String>();
        String message = "I just committed my branch";
        String message2 = "I just added these files";
        messages.add(message);
        messages.add(message2);
        assertEquals(false, messages.contains("I just committed my branch."));
    }
}
