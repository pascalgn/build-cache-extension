package com.github.pascalgn.maven.buildcache;

import org.apache.maven.eventspy.AbstractEventSpy;
import org.apache.maven.eventspy.EventSpy;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionEvent.Type;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

@Component(role = EventSpy.class)
public class EventSpyImpl extends AbstractEventSpy {
    @Requirement
    private BuildCache buildCache;

    @Requirement
    private Logger logger;

    @Override
    public void onEvent(Object event) throws Exception {
        if (event instanceof ExecutionEvent) {
            ExecutionEvent e = (ExecutionEvent) event;
            if (e.getType() == Type.SessionStarted) {
                MavenSession session = e.getSession();
                buildCache.initialize(session);
            } else if (e.getType() == Type.ProjectSucceeded) {
                buildCache.cache(e.getProject());
            }
        }
    }
}
