package it.unipr.aotlab.blogracy.web.resolvers;

import it.unipr.aotlab.blogracy.logging.Logger;
import it.unipr.aotlab.blogracy.model.hashes.Hash;
import it.unipr.aotlab.blogracy.model.users.User;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.PrintStream;
import java.io.StringWriter;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 11/7/11
 * Time: 3:38 PM
 */
public class IndexResolver extends AbstractRequestResolver {
    protected VelocityContext velocityContext = new VelocityContext();
    final static private String VIEW_NAME = "index.vm";
    final static private String VIEW_TYPE = "text/html";

    @Override
    protected void get(TrackerWebPageRequest request, TrackerWebPageResponse response) {
        setContext();
        StringWriter writer = new StringWriter();
        Template indexTemplate = loadTemplate();
        indexTemplate.initDocument();
        indexTemplate.merge(
                velocityContext,
                writer
        );
        String text = writer.toString();
        Logger.info(text);
        PrintStream ps = new PrintStream(response.getOutputStream());
        ps.print(text);
    }

    private void setContext() {
        // TODO this stuff should be parametrized or something
        velocityContext.internalPut("application", "Blogracy");
        velocityContext.internalPut("user", new User() {
            @Override
            public String getLocalNick() {
                return "The User";
            }

            @Override
            public void setLocalNick(final String nick) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Hash getHash() {
                return new Hash() {
                    String s = "42";

                    @Override
                    public String getStringValue() {
                        return s;
                    }

                    @Override
                    public byte[] getValue() {
                        return s.getBytes();
                    }
                };
            }
        });
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
