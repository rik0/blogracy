package it.unipr.aotlab.blogracy.web;

import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

/**
 * Enrico Franchi, 2011 (c)
 * <p/>
 * This program or module is released under the terms of the MIT license.
 * <p/>
 * User: enrico
 * Date: 11/2/11
 * Time: 4:38 PM
 */
public interface RequestResolver {
    void resolve(TrackerWebPageRequest request, TrackerWebPageResponse response)
            throws Exception;
}
