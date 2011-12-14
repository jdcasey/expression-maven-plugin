package org.apache.maven.plugin.expression;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.building.ModelProblemCollector;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.apache.maven.project.MavenProject;

/**
 * @goal pom
 * @requiresProject false
 * @threadSafe
 */
public class InterpolatePropertyGoal
    implements Mojo
{

    /**
     * @parameter expression="${expr}"
     */
    private String expression;

    /**
     * @parameter default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * @parameter default-value="${project}"
     */
    private MavenProject project;

    /**
     * @parameter default-value="interpolateExpression" expression="${expr.property}"
     */
    private String expressionProperty;

    /**
     * @parameter default-value="${session.executionRootDirectory}"
     * @readonly
     */
    private File executionRoot;

    /**
     * @component
     */
    private ModelInterpolator modelInterpolator;

    private Log log;

    public InterpolatePropertyGoal()
    {
        // used for plexus ONLY
    }

    public InterpolatePropertyGoal( MavenSession session, String expression )
    {
        this.expression = expression;
        this.session = session;
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

        Model model = project == null ? new Model() : project.getModel();
        File basedir = project == null ? executionRoot : project.getBasedir();

        model.getProperties().setProperty( expressionProperty, expr );

        ModelBuildingRequest request = new DefaultModelBuildingRequest();
        ModelProblemCollector problems = new LoggingModelProblemCollector( getLog() );

        Model interpolated = modelInterpolator.interpolateModel( model, basedir, request, problems );
        String value = interpolated.getProperties().getProperty( expressionProperty );

        getLog().info( "\n\nExpression: '" + expr + "'\nResolves to: '" + value + "'\n\n" );
    }

    public Log getLog()
    {
        return log == null ? new SystemStreamLog() : log;
    }

    public void setLog( Log log )
    {
        this.log = log;
    }

    private static final class LoggingModelProblemCollector
        implements ModelProblemCollector
    {
        private final Log log;

        public LoggingModelProblemCollector( Log log )
        {
            this.log = log;
        }

        public void add( Severity severity, String message, InputLocation location, Exception cause )
        {
            if ( Severity.FATAL == severity || Severity.ERROR == severity )
            {
                log.error( message + " (" + location + ")", cause );
            }
        }
    }

}
