package it.unipr.aotlab.blogracy.web.resolvers;

import it.unipr.aotlab.blogracy.errors.NotImplementedHTTPRequest;
import org.apache.velocity.VelocityContext;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 11/7/11
 * Time: 3:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class IndexResolver extends AbstractRequestResolver {
    VelocityContext velocityContext = new VelocityContext();
    private String VIEW_NAME = "index.vm";
    private String VIEW_TYPE = "text/html";

    @Override
    protected void get(TrackerWebPageRequest request, TrackerWebPageResponse response)
            throws NotImplementedHTTPRequest {

    }

    @Override
    protected String getViewName() {
        return VIEW_NAME;
    }

    @Override
    protected String getViewType() {
        return VIEW_TYPE;
    }
}
