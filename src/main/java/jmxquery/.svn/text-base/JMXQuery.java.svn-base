package jmxquery;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * 
 * JMXQuery is used for local or remote request of JMX attributes
 * It requires JRE 1.5 to be used for compilation and execution.
 * Look method main for description how it can be invoked.
 * 
 * This plugin was found on nagiosexchange.  It lacked a username/password/role system.
 * 
 * @author unknown
 * @author Ryan Gravener (<a href="http://ryangravener.com/app/contact">rgravener</a>)
 * @author Per Huss mr.per.huss (a) gmail.com
 */
public class JMXQuery
{
    private final JMXProvider jmxProvider;
    private final PrintStream out;

    private String url;
	private int verbatim;
	private JMXConnector connector;
	private MBeanServerConnection connection;
	private String warning, critical;
	private String attributeName, infoAttribute;
	private String attributeKey, infoKey;
    private String methodName;
	private String object;
	private String username, password;

    private Object defaultValue;
	
	private Object checkData;
	private Object infoData;
	
	private static final int RETURN_OK = 0; // 	 The plugin was able to check the service and it appeared to be functioning properly
	private static final String OK_STRING = "JMX OK -"; 
	private static final int RETURN_WARNING = 1; // The plugin was able to check the service, but it appeared to be above some "warning" threshold or did not appear to be working properly
	private static final String WARNING_STRING = "JMX WARNING -"; 
	private static final int RETURN_CRITICAL = 2; // The plugin detected that either the service was not running or it was above some "critical" threshold
	private static final String CRITICAL_STRING = "JMX CRITICAL -"; 
	private static final int RETURN_UNKNOWN = 3; // Invalid command line arguments were supplied to the plugin or low-level failures internal to the plugin (such as unable to fork, or open a tcp socket) that prevent it from performing the specified operation. Higher-level errors (such as name resolution errors, socket timeouts, etc) are outside of the control of plugins and should generally NOT be reported as UNKNOWN states.
	private static final String UNKNOWN_STRING = "JMX UNKNOWN";

    public JMXQuery(JMXProvider jmxProvider, PrintStream out)
    {
        this.jmxProvider = jmxProvider;
        this.out = out;
    }

    public int runCommand(String... args)
    {
        try {
            parse(args);
            connect();
            execute();
            return report(out);

        }
        catch(Exception ex) {
            return report(ex, out);
        }
        finally {
            try {
                disconnect();
            }
            catch (IOException ignore) { }
        }
    }

    private void connect() throws IOException
	{
         JMXServiceURL jmxUrl = new JMXServiceURL(url);
                  
         if(username!=null) {
        	 Map<String, String[]> m = new HashMap<String,String[]>();
        	 m.put(JMXConnector.CREDENTIALS,new String[] {username,password});
        	 connector = jmxProvider.getConnector(jmxUrl, m);
         } else {
        	 connector = jmxProvider.getConnector(jmxUrl, null);
         }
         connection = connector.getMBeanServerConnection();
	}
	

	private void disconnect() throws IOException
    {
        if(connector!=null)
        {
            connector.close();
        }
	}
	
	
	/**
     * The main method, invoked when running from command line.
	 * @param args The supplied parameters.
	 */
	public static void main(String[] args)
    {
        System.exit(new JMXQuery(new DefaultJMXProvider(), System.out).runCommand(args));
    }

    private int report(Exception ex, PrintStream out)
	{
		if(ex instanceof ParseError){
			out.print(UNKNOWN_STRING+" ");
			reportException(ex, out);		
			out.println(" Usage: check_jmx -help ");
			return RETURN_UNKNOWN;
		}else{
			out.print(CRITICAL_STRING+" ");
			reportException(ex, out);		
			out.println();
			return RETURN_CRITICAL;
		}
	}

	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private void reportException(Exception ex, PrintStream out)
    {
        out.print(verbatim < 2
                ? rootCause(ex).getMessage()
                : ex.getMessage() + " connecting to " + object + " by URL " + url);

		if(verbatim>=3)		
			ex.printStackTrace(out);
	}

	@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private static Throwable rootCause(Throwable ex)
    {
        return ex.getCause() == null ? ex : rootCause(ex.getCause());
    }


	private int report(PrintStream out)
	{
		int status;
		if(critical != null && compare( critical )){
			status = RETURN_CRITICAL;			
			out.print(CRITICAL_STRING);
		}else if (warning != null && compare( warning)){
			status = RETURN_WARNING;
			out.print(WARNING_STRING);
		}else{
			status = RETURN_OK;
			out.print(OK_STRING);
		}

        boolean shown = false;
        if(infoData==null || verbatim>=2){
            out.print(' ');
            if(attributeKey !=null) {
                out.print(attributeName +'.'+ attributeKey +"="+checkData);
                if ( checkData instanceof Number) {
                    out.print (" | "+ attributeName +'.'+ attributeKey +"="+checkData);
                }
            }
            else {
                out.print(attributeName +"="+checkData);
                if ( checkData instanceof Number) {
                    out.print (" | "+ attributeName +"="+checkData);
                }
                shown=true;
            }
        }

		if(!shown && infoData!=null){
			if(infoData instanceof CompositeDataSupport)
				report((CompositeDataSupport)infoData, out);
			else
				out.print(infoData.toString());
		}
		
		out.println();
		return status;
	}

	@SuppressWarnings("unchecked")
	private void report(CompositeDataSupport data, PrintStream out) {
		CompositeType type = data.getCompositeType();
		out.print(",");
		for(Iterator it = type.keySet().iterator();it.hasNext();){
			String key = (String) it.next();
			if(data.containsKey(key))
				out.print(key+'='+data.get(key));
			if(it.hasNext())
				out.print(';');
		}
	}


	private boolean compare(String level) {		
		if(checkData instanceof Number) {
			Number check = (Number)checkData;
			if(check.doubleValue()==Math.floor(check.doubleValue())) {
				return check.doubleValue()>=Double.parseDouble(level);
			} else {
				return check.longValue()>=Long.parseLong(level);
			}
		}
		if(checkData instanceof String) {
			return checkData.equals(level);
		}
		if(checkData instanceof Boolean) {
			return checkData.equals(Boolean.parseBoolean(level));
		}
		throw new RuntimeException(level + "is not of type Number,String or Boolean");
	}


	private void execute() throws Exception
    {
        Object value;
        try {
            value = attributeName != null
                           ? connection.getAttribute(new ObjectName(object), attributeName)
                           : connection.invoke(new ObjectName(object), methodName, null, null);
        }
        catch(OperationsException e)
        {
            if(defaultValue != null)
                value = defaultValue;
            else
                throw e;
        }

		if(value instanceof CompositeDataSupport) {
            if(attributeKey ==null) {
                throw new ParseError("Attribute key is null for composed data "+object);
            }
            checkData = ((CompositeDataSupport) value).get(attributeKey);
		}
        else {
			checkData = value;
		}
		
		if(infoAttribute !=null){
			Object infoValue = infoAttribute.equals(attributeName)
                    ? value :
                    connection.getAttribute(new ObjectName(object), infoAttribute);
			if(infoKey !=null && (infoValue instanceof CompositeDataSupport) && verbatim<4){
                infoData = ((CompositeDataSupport) value).get(infoKey); // todo: Possible bug? value <=> infoValue
			}
            else {
				infoData = infoValue;
			}
		}
	}


	private void parse(String[] args) throws ParseError
	{
		try{
			for(int i=0;i<args.length;i++){
				String option = args[i];
				if(option.equals("-help"))
				{
					printHelp(System.out);
					System.exit(RETURN_UNKNOWN);
				}
                else if(option.equals("-U")) {
					url = args[++i];
				}
                else if(option.equals("-O")) {
					object = args[++i];
				}
                else if(option.equals("-A")) {
					attributeName = args[++i];
				}
                else if(option.equals("-I")) {
					infoAttribute = args[++i];
				}
                else if(option.equals("-J")) {
					infoKey = args[++i];
				}
                else if(option.equals("-K")) {
					attributeKey = args[++i];
				}
                else if(option.equals("-M")) {
                    methodName = args[++i];
                }
                else if(option.startsWith("-v")) {
					verbatim = option.length()-1;
				}
                else if(option.equals("-w")) {
					warning = args[++i];
				}
                else if(option.equals("-c")) {
					critical = args[++i];
				}
                else if(option.equals("-username")) {
					username = args[++i];
				}
                else if(option.equals("-password")) {
					password = args[++i];
				}
                else if(option.equals("-default")) {
                    String strValue = args[++i];
                    try {
                        defaultValue = Long.valueOf(strValue);
                    }
                    catch(NumberFormatException e)
                    {
                        defaultValue = strValue;
                    }
                }
			}
			
            if(url == null || object == null || (attributeName == null && methodName == null))
                throw new Exception("Required options not specified");
		}
        catch(Exception e) {
			throw new ParseError(e);
		}
	}


	private void printHelp(PrintStream out) {
		InputStream is = JMXQuery.class.getClassLoader().getResourceAsStream("jmxquery/HELP");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		try{
			while(true){
				String s = reader.readLine();
				if(s==null)
					break;
				out.println(s);
			}
		} catch (IOException e) {
			out.println(e);
		}finally{
			try {
				reader.close();
			} catch (IOException e) {
				out.println(e);
			}
		}	
	}
}
