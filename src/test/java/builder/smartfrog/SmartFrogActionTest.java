package builder.smartfrog;

import hudson.model.AbstractBuild;
import hudson.model.FreeStyleProject;
import hudson.model.listeners.RunListener;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.zip.GZIPInputStream;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class SmartFrogActionTest {
    
    @Rule
    public JenkinsRule j = new JenkinsRule();
    
    @Test
    public void encodeIPv6Hostname() {
        SmartFrogAction sfa = new SmartFrogAction(null, "::1");
        assertEquals("console-%3A%3A1", sfa.getUrlName());
    }
    
    @Test
    public void testLogCompression() throws Exception{
        FreeStyleProject project = j.createFreeStyleProject();
        j.buildAndAssertSuccess(project);
        AbstractBuild run = project.getLastBuild();
        SmartFrogAction action = new SmartFrogAction(null,"localhost");
        action.setBuild(run);
        run.addAction(action);
        SmartFrogBuildListener listener = RunListener.all().get(SmartFrogBuildListener.class);
        PrintStream st = new PrintStream(action.getLogFile());
        st.println("testing log");
        st.close();
        listener.onFinalized(run);
        assertTrue("Log is not compressed.", action.getLogFile().getName().equals("localhost.log.gz") && action.getLogFile().exists());
        GZIPInputStream inputStream = new GZIPInputStream(new FileInputStream(action.getLogFile()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String log = reader.readLine();
        reader.close();
        assertEquals("Compressed log does not contain original log.", "testing log", log);
        
    }

}
