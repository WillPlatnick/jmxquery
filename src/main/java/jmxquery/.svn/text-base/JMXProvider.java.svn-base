package jmxquery;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.Map;

/**
 * Defines a provider of JMXConnector objects.
 */
public interface JMXProvider
{
    /**
     * Returns a connected JMXConnector.
     * @param url The JMX service url.
     * @param env The environment properties.
     * @return A connected JMXConnector, never null.
     * @throws IOException On connection problems.
     *
     * @see javax.management.remote.JMXConnectorFactory#connect
     */
    JMXConnector getConnector(JMXServiceURL url, Map<String, ?> env) throws IOException;
}
