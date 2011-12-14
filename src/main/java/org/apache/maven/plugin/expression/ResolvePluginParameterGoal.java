package org.apache.maven.plugin.expression;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginParameterExpressionEvaluator;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;

/**
 * @goal plugin
 * @requiresProject false
 * @threadSafe
 */
public class ResolvePluginParameterGoal
    implements Mojo
{

    /**
     * @parameter expression="${expr}"
     */
    private String expression;

    /**
     * @parameter default-value="${mojoExecution}"
     * @required
     * @readonly
     */
    private MojoExecution mojoExecution;

    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    private Log log;

    public ResolvePluginParameterGoal()
    {
        // used for plexus ONLY
    }

    public ResolvePluginParameterGoal( MavenSession session, MojoExecution mojoExecution, String expression )
    {
        this.mojoExecution = mojoExecution;
        this.expression = expression;
        this.session = session;
    }

    public MojoExecution getMojoExecution()
    {
        return mojoExecution;
    }

    public void setMojoExecution( MojoExecution mojoExecution )
    {
        this.mojoExecution = mojoExecution;
    }

    public MavenSession getSession()
    {
        return session;
    }

    public void setSession( MavenSession session )
    {
        this.session = session;
    }

    public String getExpression()
    {
        return expression;
    }

    public void setExpression( String expression )
    {
        this.expression = expression;
    }

    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        String expr = expression;
        if ( expr == null )
        {
            BufferedReader in = null;
            while ( expr == null && session.getSettings().isInteractiveMode() )
            {
                if ( in == null )
                {
                    in = new BufferedReader( new InputStreamReader( System.in ) );
                }

                System.out.print( "Enter an expression (CTL-C to end): " );
                try
                {
                    expr = in.readLine();
                }
                catch ( IOException e )
                {
                    throw new MojoExecutionException( "Cannot read from System.in", e );
                }

                if ( expr.trim().length() < 4 )
                {
                    expr = null;
                    continue;
                }

                resolve( expr );
                expr = null;
            }
        }
        else
        {
            resolve( expr );
        }

    }

    private void resolve( String expr )
        throws MojoExecutionException
    {
        if ( expr.indexOf( "${" ) < 0 )
        {
            expr = "${" + expr;
        }

        if ( expr.indexOf( '}' ) < 2 )
        {
            expr += "}";
        }

        try
        {
            Object result = new PluginParameterExpressionEvaluator( session, this.mojoExecution ).evaluate( expr );

            getLog().info( "\n\nExpression: '"
                               + expression
                               + "'\nResolves to: "
                               + ( result == null ? "-NULL-" : "'" + result + "'\n(Resulting class: "
                                   + result.getClass().getName() + ")" ) + "\n\n" );
        }
        catch ( ExpressionEvaluationException e )
        {
            throw new MojoExecutionException( "Failed to resolve plugin parameter expression: '" + expression
                + "'. Reason: " + e.getMessage(), e );
        }
    }

    public Log getLog()
    {
        return log == null ? new SystemStreamLog() : log;
    }

    public void setLog( Log log )
    {
        this.log = log;
    }

}
