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

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.logging.Logger;
import it.unipr.aotlab.blogracy.web.url.ConfigurationTimeParameters;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;

@ConfigurationTimeParameters({String.class})
public class StaticFileResolver implements RequestResolver {
    private String staticFilesDirectory;

    public StaticFileResolver(final String staticFilesDirectory) throws ServerConfigurationError {
        checksValidStaticRootAndSetField(staticFilesDirectory);
    }

    public StaticFileResolver(final String staticFilesDirectory,
                              final String ignoredPseudoUrl) throws ServerConfigurationError {
        throw new ServerConfigurationError("Use non capturing regex in the server configuration!");
    }

    /**
     * Checks if {@param staticRoot} exists and is a directory
     *
     * @param staticRoot is the path to check
     * @throws ServerConfigurationError if {@param staticRoot} does not exist or is not a directory
     */
    private void checksValidStaticRootAndSetField(final String staticRoot)
            throws ServerConfigurationError {
        File staticRootFile = new File(staticRoot);
        if (staticRootFile.exists()) {
            if (staticRootFile.isDirectory()) {
                staticFilesDirectory = staticRoot;
            } else {
                throw new ServerConfigurationError(
                        errorMessageNotDirectory(staticRoot));
            }
        } else {
            throw new ServerConfigurationError(
                    errorMessageNotExists(staticRoot));
        }
    }

    private static String errorMessageNotExists(final String staticRoot) {
        return "Static files root " + staticRoot + " does not exist.";
    }

    private static String errorMessageNotDirectory(final String staticRoot) {
        return "Static files root " + staticRoot + " exists but is not a directory.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(
            final TrackerWebPageRequest request,
            final TrackerWebPageResponse response)
            throws URLMappingError {
        /*
        * Keep in mind that due to the underlying TrackerWebPageResponseImpl#useFile(String, String)
        * implementation, files without extension cannot be processed (why developers assume everybody is using
        * windows anyway?).
        *
        * If someone feels a compelling reason to resolve a file without extension, he may wrote some crazy code
        * to run around the default implementation of TrackerWebPageResponseImpl, like copy the file to a file with
        * extension and  send that. Then they could go and meet the original developers with an {@code Object}
        * implementing  the {@code Bludgeoning} interface and repeatedly call {@code Bludgeoning#hitOnTheHead} passing
        * the aforementioned developers as actual parameters.
        *
        * Ah, when there is no doc, we are always obliged to read the implementations. So perhaps some day this will
        * not be true anymore.
        *
        * Note to self: when you suppose someone else is reading your code and you are doing
        * some low level stuff like manually checking if a file has an extension, do not use explicative names like
        * {@code fileHasExtension(filename)} and similar over-engineering techniques: since your code is worth being read only by
        * true hackers, rather leave the low level stuff and use an equivalent 6502 asm program to explain your goals.
        *
        * Besides, actually TrackerWebPageResponseImpl#useFile(String, String)
        * does set the right {@code Content-Type} headers. Well, <i>right</i> according to the hash table
        * hardcoded in {@link com.aelitis.azureus.core.util.HTTPUtils}. If your file do not fit I would just
        * suggest the {@code Bludgeoning} trick once again. My whole idea of having a pluggable mime-type resolver
        * is just for fools who think there are more than 27 or so different file-types.
        *
        * Since {@link com.aelitis.azureus.core.util.HTTPUtils#guessContentTypeFromFileType(String)} returns
        * the argument if there is no match and that is what is called by
        * {@link org.gudy.azureus2.pluginsimpl.local.tracker.TrackerWebPageResponseImpl#useStream(String, java.io.InputStream)}
        * to resolve the first String parameter, you may want to use a pluggable mime-type resolver with the "right"
        * mime-type (which being no extension will be used as the true content type). Have bloody fun with it.
        */

        final String url = request.getURL();

        try {
            boolean didSendTheFile = response.useFile(staticFilesDirectory, url);
            if (!didSendTheFile) {
                if (mayBeDirectoryUrl(url)) {
                    redirectToIndexInDirectory(url, response);
                } else {
                    throw new URLMappingError(
                            HttpURLConnection.HTTP_NOT_FOUND,
                            "Could not find " + url
                    );
                }
            }
        } catch (FileNotFoundException e) {
            throw new URLMappingError(HttpURLConnection.HTTP_NOT_FOUND, e);
        } catch (SecurityException e) {
            throw new URLMappingError(HttpURLConnection.HTTP_FORBIDDEN, e);
        } catch (IOException e) {
            /* we should not get here */
            throw new URLMappingError(HttpURLConnection.HTTP_NOT_FOUND, e);
        }

    }

    private void redirectToIndexInDirectory(final String url, final TrackerWebPageResponse response) {
        Logger.info(url + " may be a directory. Trying to send index.html instead.");
        response.setReplyStatus(HttpURLConnection.HTTP_SEE_OTHER);
        response.setHeader("Location", url + "index.html");
    }

    private boolean mayBeDirectoryUrl(final String url) {
        return url.endsWith("/");
    }

    private File getFileSystemPath(final String url) {
        return new File(staticFilesDirectory, url);
    }

    public boolean couldResolve(final String url) {
        File tentativeFile = getFileSystemPath(url);
        return tentativeFile.exists();
    }
}
