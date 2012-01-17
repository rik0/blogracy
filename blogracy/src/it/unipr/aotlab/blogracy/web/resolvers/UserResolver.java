/*
 * Copyright (c)  2011 Enrico Franchi.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package it.unipr.aotlab.blogracy.web.resolvers;

import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.model.hashes.Hashes;
import it.unipr.aotlab.blogracy.model.users.User;
import it.unipr.aotlab.blogracy.model.users.Users;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

public class UserResolver extends VelocityRequestResolver {
    final static private String VIEW_NAME = "user.vm";
    final static private String VIEW_TYPE = "text/html";

    private String userName;
    private User user;

    public UserResolver(String userName) {
        this.userName = userName;
        user = Users.newUser(Hashes.newHash(userName));

    }

    @Override
    protected void get(final TrackerWebPageRequest request, final TrackerWebPageResponse response) throws URLMappingError {
        super.velocityGet(response);
    }

    @Override
    protected void setupContext() {
        velocityContext.internalPut("application", "Blogracy");
        velocityContext.internalPut("user", user);
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
