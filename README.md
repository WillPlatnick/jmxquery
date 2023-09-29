jmxquery
========

A nagios/icinga plugin to query JMX values - Forked from https://code.google.com/p/jmxquery/ 
Then forked again from https://github.com/WillPlatnick/jmxquery

Why Forked?
==========

The development doesn't seem to be active and issues have been hanging around for years. I needed to make some changes to get jmxquery to work properly for what I needed, so I have thrown my changes into this repo.

What's New?
==========

Requires Java 11 or later to compile.

Updated 3rd party dependencies.

The check_jmx shell script will now use the java from your $PATH and will pass in spaces, now allowing you to query JMX objects with spaces in them

Command Line Option -s: This will raise a critical alert if the returned value equals the string provided to -s

Command Line Option -ns: This will raise a critical alert if the returned value doesn't equal the string provided to -ns

Plugin
======

If you just want the files needed for the plugin to work, just grab them from the plugin directory. Just copy both of the files from plugin/ into your nagios plugins directory

Usage Example
=============

./check_jmx -U service:jmx:rmi:///jndi/rmi://localhost:3637/jmxrmi -O org.neo4j:instance=kernel#0,"name=High Availability" -A Alive -ns true


