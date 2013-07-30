package jmxquery;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import static javax.management.remote.JMXConnector.CREDENTIALS;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for the JMXQuery class.
 */
public class JMXQueryTest
{
    /** The subject under test */
    private JMXQuery jmxQuery;

    /** The output stream the output is written to */
    private ByteArrayOutputStream outputStream;

    /** The return status of the command */
    private int status;

    /** Mocked JMXProvider */
    @Mock private JMXProvider jmxProvider;

    /** Mocked JMXConnector */
    @Mock private JMXConnector jmxConnector;

    /** Mocked MBeanServerConnection */
    @Mock private MBeanServerConnection mBeanServerConnection;

    /**
     * Prepares the mocks, buffers and test subject.
     */
    @BeforeMethod
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        when(jmxProvider.getConnector(any(JMXServiceURL.class), any(Map.class))).thenReturn(jmxConnector);
        when(jmxConnector.getMBeanServerConnection()).thenReturn(mBeanServerConnection);

        outputStream = new ByteArrayOutputStream();
        jmxQuery = new JMXQuery(jmxProvider, new PrintStream(outputStream));
    }

    /**
     * Local helper to run command with args as a single string.
     * @param args The space-separated arguments in a single string.
     */
    private void runCommand(String args)
    {
        status = jmxQuery.runCommand(args.split(" "));
    }

    /**
     * Result assertion helper method.
     * @param status The expected return status.
     * @param message The expected output string.
     */
    private void assertResponseWas(int status, String message)
    {
        assertEquals(this.status, status);
        assertEquals(outputStream.toString(), message + "\n");
    }

    @Test(description = "Test reading a simple numeric attribute.")
    public void testReadSimpleNumericAttribute() throws Exception
    {
        when(mBeanServerConnection.getAttribute(eq(ObjectName.getInstance("foo:bar=x")), eq("baz"))).thenReturn(42);
        runCommand("-U service:jmx:some://domain.com -O foo:bar=x -A baz");
        assertResponseWas(0, "JMX OK - baz=42 | baz=42");
    }

    @Test(description = "Test reading a simple numeric attribute on warn level.")
    public void testWarnOnSimpleAttribute() throws Exception
    {
        when(mBeanServerConnection.getAttribute(eq(ObjectName.getInstance("foo:bar=x")), eq("baz"))).thenReturn(42);
        runCommand("-U service:jmx:some://domain.com -O foo:bar=x -A baz -w 25 -c 50");
        assertResponseWas(1, "JMX WARNING - baz=42 | baz=42");
    }

    @Test(description = "Test invoking a zero-argument void method.")
    public void testInvokeMethod() throws Exception
    {
        runCommand("-U service:jmx:some://domain.com -O foo:bar=x -M test");
        verify(mBeanServerConnection).invoke(ObjectName.getInstance("foo:bar=x"), "test", null, null);
        assertResponseWas(0, "JMX OK - null=null");
    }

    @Test(description = "Test credentials used.")
    @SuppressWarnings({"unchecked", "RedundantCast"})
    public void testCredentialsUsed() throws IOException
    {
        runCommand("-U service:jmx:some://domain.com -O foo:bar=x -A test -username duke -password java_rulez");
        ArgumentCaptor<Map<String, ?>> envCaptor
                = (ArgumentCaptor<Map<String,?>>) (ArgumentCaptor<?>) ArgumentCaptor.forClass(Map.class);
        verify(jmxProvider).getConnector(any(JMXServiceURL.class), envCaptor.capture());
        assertEquals(envCaptor.getValue().get(CREDENTIALS), new String[] { "duke", "java_rulez"});
    }

    @Test(description = "Default value if attribute doesn't exist.")
    public void testDefaultValue() throws Exception
    {
        when(mBeanServerConnection.getAttribute(any(ObjectName.class), anyString())).thenThrow(new InstanceNotFoundException());
        runCommand("-U service:jmx:some://domain.com -O foo:bar=x -A baz -default -1");
        assertResponseWas(0, "JMX OK - baz=-1 | baz=-1");
    }

    @Test(description = "Default value if attribute doesn't exist.")
    public void testDefaultValueNotNeeded() throws Exception
    {
        when(mBeanServerConnection.getAttribute(any(ObjectName.class), anyString())).thenReturn(2);
        runCommand("-U service:jmx:some://domain.com -O foo:bar=x -A baz -default -1");
        assertResponseWas(0, "JMX OK - baz=2 | baz=2");
    }
}
