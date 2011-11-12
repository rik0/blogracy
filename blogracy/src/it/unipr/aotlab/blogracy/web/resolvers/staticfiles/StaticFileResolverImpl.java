/*
 * Copyright (c)  2011 Enrico Franchi, Michele Tomaiuolo and University of Parma.
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

package it.unipr.aotlab.blogracy.web.resolvers.staticfiles;

import it.unipr.aotlab.blogracy.errors.ServerConfigurationError;
import it.unipr.aotlab.blogracy.errors.URLMappingError;
import it.unipr.aotlab.blogracy.logging.Logger;
import it.unipr.aotlab.blogracy.mime.MimeFinder;
import it.unipr.aotlab.blogracy.mime.MimeFinderFactory;
import it.unipr.aotlab.blogracy.util.FileUtils;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageRequest;
import org.gudy.azureus2.plugins.tracker.web.TrackerWebPageResponse;

import java.io.*;
import java.net.HttpURLConnection;

/**
 * User: enrico
 * Package: it.unipr.aotlab.blogracy.web.resolvers
 * Date: 11/11/11
 * Time: 4:10 PM
 */
public class StaticFileResolverImpl implements StaticFileResolver {
    MimeFinder mimeFinder = MimeFinderFactory.getInstance();
    private File staticFilesDirectory;

    StaticFileResolverImpl(final File staticFilesDirectory) throws ServerConfigurationError {
        checksValidStaticRootAndSetField(staticFilesDirectory);
    }

    StaticFileResolverImpl(final File staticFilesDirectory, MimeFinder mimeFinder)
            throws ServerConfigurationError {
        this(staticFilesDirectory);
        this.mimeFinder = mimeFinder;
    }

    /**
     * Checks if {@param staticRoot} exists and is a directory
     *
     * @param staticRoot is the path to check
     * @throws ServerConfigurationError if {@param staticRoot} does not exist or is not a directory
     */
    private void checksValidStaticRootAndSetField(final File staticRoot)
            throws ServerConfigurationError {
        if (staticRoot.exists()) {
            if (staticRoot.isDirectory()) {
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

    private static String errorMessageNotExists(final File staticRoot) {
        return "Static files root " +
                staticRoot.toString() +
                " does not exist.";
    }

    private static String errorMessageNotDirectory(final File staticRoot) {
        return "Static files root " +
                staticRoot.toString() +
                " exists but is not a directory.";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resolve(
            final TrackerWebPageRequest request,
            final TrackerWebPageResponse response)
            throws URLMappingError, IOException {
        final String url = request.getURL();
        final File actualFile = getFileSystemPath(url);
        final String staticFilesDirectoryName = staticFilesDirectory.getAbsolutePath();

        try {
            response.useFile(staticFilesDirectoryName, url);
        } catch (IOException e) {
            if (actualFile.isDirectory()) {
                Logger.info(actualFile.getAbsolutePath()
                        + " is a directory. Trying to send index.html instead.");
                response.setReplyStatus(HttpURLConnection.HTTP_SEE_OTHER);
                response.setHeader("Location", url + "/index.html");
            } else {
                throw new URLMappingError(HttpURLConnection.HTTP_NOT_FOUND, e);

            }
        }
    }

    @Override
    public String resolvePath(final String url) throws URLMappingError {
        final File fileSystemPath = getFileSystemPath(url);
        if (fileSystemPath.exists()) {
            return fileSystemPath.getAbsolutePath();
        } else {
            throw new URLMappingError(
                    HttpURLConnection.HTTP_NOT_FOUND,
                    "Could not find file for " + fileSystemPath.getAbsolutePath()
            );
        }
    }

    private void sendFile(final File actualFile,
                          final OutputStream outputStream) throws IOException {
        FileUtils.copyCompletely(
                new FileReader(actualFile),
                new OutputStreamWriter(outputStream)
        );
        outputStream.flush();
        outputStream.close();
    }

    private File getFileSystemPath(final String url) {
        return new File(staticFilesDirectory, url);
    }

    @Override
    public boolean couldResolve(final String url) {
        File tentativeFile = getFileSystemPath(url);
        return tentativeFile.exists();
    }
}
