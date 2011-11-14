package it.unipr.aotlab.blogracy.web.resolvers;

import it.unipr.aotlab.blogracy.model.hashes.Hash;
import it.unipr.aotlab.blogracy.model.users.User;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

/**
 * MainResolver presents the main application page.
 */
public class MainResolver extends VelocityRequestResolver {
    final static private String VIEW_NAME = "index.vm";
    final static private String VIEW_TYPE = "text/html";

    @Override
    protected void get(TrackerWebPageRequest request, TrackerWebPageResponse response) {
        velocityGet(response);
    }


    @Override
    protected String getViewName() {
        return VIEW_NAME;
    }

    @Override
    protected String getViewType() {
        return VIEW_TYPE;
    }

    @Override
    protected void setupContext() {
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
}
