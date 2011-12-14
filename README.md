Maven Expression Plugin
=======================

Overview
--------

The Maven Expression Plugin is intended to be a development aid for plugin developers and build engineers. Its main purpose is to allow users to resolve expressions interactively, as if they were being resolved for injection as a plugin parameter, or interpolated into a `pom.xml` file. Each goal works in two ways: single-execution, and fully interactive. The single-execution mode takes a parameter on the Maven command line (`-Dexpr=foo`), resolves it, reports the result, and quits. On the other hand, the fully interactive mode kicks in if you *don't* use the `expr` command-line parameter, and will continue to prompt you for input (and report the resolution result) until you hit CTL-C. This allows you to explore multiple parameter expressions without having to constantly edit and re-run a complex command line.

There are currently two goals: `expression:plugin` and `expression:pom`. See below for an explanation of each.


Using
-----

To use the expression plugin via the `expression:` goal prefix, you'll need to add the `org.commonjava.maven.plugins` groupId to your settings.xml:

    <?xml version="1.0" encoding="UTF-8"?>
    <settings>
      [...]
      <pluginGroups>
        <pluginGroup>org.commonjava.maven.plugins</pluginGroups>
      </pluginGroups>
      [...]
    </settings>

Note that the standard plugin groups (`org.apache.maven.plugins` and `org.codehaus.mojo`) will be implied, even with the above `<pluginGroups/>` configuration.

`expression:plugin`
-------------------

The `plugin` goal is used to resolve an expression as if it were going to be injected as a plugin parameter. It will use the projects in the current session, if there are any, and will expose access to the `MojoExecution` instance used to execute it if an expression references `${mojoExecution}`.

The following example uses the `plugin` goal in single-execution mode to resolve the `${session.executionRootDirectory}` expression:

    $ mvn expression:plugin -Dexpr=session.executionRootDirectory
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Maven Expressions Plugin 0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- maven-expression-plugin:1.0-SNAPSHOT:plugin (default-cli) @ expression-maven-plugin ---
    [INFO] 
    
    Expression: '${session.executionRootDirectory}'
    Resolves to: '/Users/jdcasey/workspace/maven-expression-plugin'
    (Resulting class: java.lang.String)
    
    
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 0.503s
    [INFO] Finished at: Wed Dec 14 15:26:11 EST 2011
    [INFO] Final Memory: 3M/81M
    [INFO] ------------------------------------------------------------------------

And this is the same example, run in fully interactive mode:

    $ mvn expression:plugin
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Maven Expressions Plugin 0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- expression-maven-plugin:0.1-SNAPSHOT:plugin (default-cli) @ expression-maven-plugin ---
    Enter an expression (CTL-C to end): session.executionRootDirectory
    [INFO] 
    
    Expression: '${session.executionRootDirectory}'
    Resolves to: '/Users/jdcasey/workspace/redhat/maven-expression-plugin'
    (Resulting class: java.lang.String)
    
    
    Enter an expression (CTL-C to end): project.build.directory
    [INFO] 
    
    Expression: '${project.build.directory}'
    Resolves to: '/Users/jdcasey/workspace/redhat/maven-expression-plugin/target'
    (Resulting class: java.lang.String)
    
    
    Enter an expression (CTL-C to end): ^C
    $

As you can see, using the fully interactive mode allows us to resolve more than one expression without restarting.

Additionally, it's possible to use command-line parameters in your expressions:

    $ mvn expression:plugin -Dfoo=bar
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Maven Expressions Plugin 0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- expression-maven-plugin:0.1-SNAPSHOT:plugin (default-cli) @ expression-maven-plugin ---
    Enter an expression (CTL-C to end): ${project.build.directory}/${foo}
    [INFO] 
    
    Expression: '${project.build.directory}/${foo}'
    Resolves to: '/Users/jdcasey/workspace/redhat/maven-expression-plugin/target/bar'
    (Resulting class: java.lang.String)


`expression:pom`
----------------

The `pom` goal operates in exactly the same way as the `plugin` goal, with one critical difference: Rather than resolving expressions as if they were being injected as plugin parameters, the `pom` goal resolves expressions as if they were being interpolated into a `pom.xml` file. This is the method used to interpolate expressions in resources, dependencies, basically anything outside of a plugin `<configuration/>` section. This means that some of the dynamic build state available to the plugin-parameter expression resolver will _not_ be available here.

For example, the `project.build.directory` path resolves in both places (compare with the `plugin` goal, above):

    $ mvn expression:pom
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Maven Expressions Plugin 0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- expression-maven-plugin:0.1-SNAPSHOT:pom (default-cli) @ expression-maven-plugin ---
    Enter an expression (CTL-C to end): project.build.directory
    [INFO] 
    
    Expression: '${project.build.directory}'
    Resolves to: '/Users/jdcasey/workspace/redhat/maven-expression-plugin/target'

However, expressions based on session information (which is dynamic) doesn't resolve with the `pom` goal:

    $ mvn expression:pom
    [INFO] Scanning for projects...
    [INFO]                                                                         
    [INFO] ------------------------------------------------------------------------
    [INFO] Building Maven Expressions Plugin 0.1-SNAPSHOT
    [INFO] ------------------------------------------------------------------------
    [INFO] 
    [INFO] --- expression-maven-plugin:0.1-SNAPSHOT:pom (default-cli) @ expression-maven-plugin ---
    Enter an expression (CTL-C to end): session.executionRootDirectory
    [INFO] 
    
    Expression: '${session.executionRootDirectory}'
    Resolves to: '${session.executionRootDirectory}'

