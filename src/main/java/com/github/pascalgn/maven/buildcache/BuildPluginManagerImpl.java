package com.github.pascalgn.maven.buildcache;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.LoggerManager;

@Component(role = BuildPluginManager.class)
public class BuildPluginManagerImpl extends DefaultBuildPluginManager {
    @Requirement
    private BuildCache buildCache;

    @Requirement
    private LoggerManager loggerManager;

    @Override
    public void executeMojo(MavenSession session, MojoExecution mojoExecution) throws MojoFailureException,
            MojoExecutionException, PluginConfigurationException, PluginManagerException {
        MavenProject project = session.getCurrentProject();
        if (project != null && buildCache.isCached(project)) {
            Logger logger = loggerManager.getLoggerForComponent(mojoExecution.getMojoDescriptor().getImplementation());
            logger.info("Execution skipped, cached artifact will be used!");
        } else {
            super.executeMojo(session, mojoExecution);
        }
    }
}
