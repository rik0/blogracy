/*
 * Copyright (c)  2012 Enrico Franchi, Michele Tomaiuolo and University of Parma.
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

package net.blogracy.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import net.blogracy.model.hashes.Hashes;
import net.blogracy.model.users.User;
import net.blogracy.model.users.Users;

/**
 * User: enrico Package: net.blogracy.config Date: 1/24/12 Time: 11:37 AM
 */
public class Configurations {

    public static final String BLOGRACY = "blogracy";
    private static final String PATHS_FILE = "blogracyPaths.properties";
    private static final String BLOGRACY_PATHS_STATIC = "blogracy.paths.static";
    private static final String BLOGRACY_PATHS_CACHED = "blogracy.paths.cache";
    private static final String BLOGRACY_PATHS_TEMPLATES = "blogracy.paths.templates";
    private static final String BLOGRACY_PATHS_ROOT = "blogracy.paths.root";
    private static final String VUZE_FILE = "blogracyVuze.properties";
    private static final String BLOGRACY_VUZE_PORT = "blogracy.vuze.port";
    private static final String BLOGRACY_VUZE_BROKER = "blogracy.vuze.broker";
    private static final String USER_FILE = "blogracyUser.properties";
    private static final String BLOGRACY_USER_USER = "blogracy.user.user";
    private static final String BLOGRACY_USER_FRIENDS = "blogracy.user.friends";

    static private Properties loadProperties(String file) throws IOException {
        InputStream is = getResourceAsStream(file);
        if (is != null) {
            Properties properties = new Properties();
            properties.load(is);
            return properties;
        } else {
            return new Properties();
        }
    }

    static private InputStream getResourceAsStream(String file)
            throws IOException {
        InputStream is = null;
        File f = new File(file);
        if (f.exists()) {
            is = new FileInputStream(f);
        } else {
            is = Configurations.class.getClassLoader()
                    .getResourceAsStream(file);
        }
        return is;
    }

    static public PathConfig getPathConfig() {
        try {

            return new PathConfig() {
                // TODO: this should absolutely come from the outside!
                Properties pathProperties = loadProperties(PATHS_FILE);

                @Override
                public String getStaticFilesDirectoryPath() {
                    return pathProperties.getProperty(BLOGRACY_PATHS_STATIC);
                }

                @Override
                public String getCachedFilesDirectoryPath() {
                    String cachedFilesDirectoryPath = pathProperties
                            .getProperty(BLOGRACY_PATHS_CACHED);
                    // "Lazy" creation of cached files folder if non-existent
                    File f = new File(cachedFilesDirectoryPath);
                    this.createDirIfMissing(f);
                    return f.getAbsolutePath();
                }

                @Override
                public String getTemplatesDirectoryPath() {
                    return pathProperties.getProperty(BLOGRACY_PATHS_TEMPLATES);
                }

                @Override
                public String getRootDirectoryPath() {
                    return pathProperties.getProperty(BLOGRACY_PATHS_ROOT);
                }

                private void createDirIfMissing(File dir) {
                    if (!dir.exists()) {
                        boolean createdDir = dir.mkdir();
                        assert (createdDir);
                    }
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public VuzeConfig getVuzeConfig() {
        try {
            return new VuzeConfig() {
                // TODO: this should absolutely come from the outside!
                Properties userProperties = loadProperties(VUZE_FILE);

                @Override
                public int getPort() {
                    String port = userProperties
                            .getProperty(BLOGRACY_VUZE_PORT);
                    return Integer.parseInt(port);
                }

                @Override
                public String getBroker() {
                    String broker = userProperties
                            .getProperty(BLOGRACY_VUZE_BROKER);
                    return broker;
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    static public UserConfig getUserConfig() {
        try {
            return new UserConfig() {
                // TODO: this should absolutely come from the outside!
                Properties userProperties = loadProperties(USER_FILE);

                @Override
                public User getUser() {
                    String userRow = userProperties
                            .getProperty(BLOGRACY_USER_USER);
                    return (userRow == null) ? null : loadUser(userRow);
                }

                @Override
                public List<User> getFriends() {
                    String userRow = userProperties
                            .getProperty(BLOGRACY_USER_USER);
                    ArrayList<User> friends = new ArrayList<User>();
                    int i = 1;
                    String friendRow = userProperties
                            .getProperty(BLOGRACY_USER_FRIENDS + '.' + i);
                    while (friendRow != null) {
                        if (!friendRow.equals(userRow)) {
                            friends.add(loadUser(friendRow));
                        }
                        ++i;
                        friendRow = userProperties
                                .getProperty(BLOGRACY_USER_FRIENDS + '.' + i);
                    }
                    return friends;
                }

                public User getFriend(final String hash) {
                    for (User friend : this.getFriends())
                        if (friend.getHash().toString().equals(hash))
                            return friend;
                    return null;
                }

                private User loadUser(String text) {
                    String[] hashAndNick = text.split(" ", 2);
                    User user = Users
                            .newUser(Hashes.fromString(hashAndNick[0]));
                    if (hashAndNick.length == 2) {
                        user.setLocalNick(hashAndNick[1]);
                    } else {
                        user.setLocalNick(hashAndNick[0]);
                    }
                    return user;
                }

                @Override
                public KeyPair getUserKeyPair() {
                    KeyPair result = null;
                    try {
                        String alias = getUser().getLocalNick();
                        char[] password = new char[] { 'b', 'l', 'o', 'g', 'r',
                                'a', 'c', 'y' };
                        InputStream is = getResourceAsStream("blogracy.jks");
                        KeyStore keyStore = KeyStore.getInstance(KeyStore
                                .getDefaultType());
                        keyStore.load(is, password);
                        is.close();
                        // Get private key
                        Key key = keyStore.getKey(alias, password);
                        if (key instanceof PrivateKey) {
                            // Get certificate of public key
                            java.security.cert.Certificate cert = keyStore
                                    .getCertificate(alias);

                            // Get public key
                            PublicKey publicKey = cert.getPublicKey();

                            // Return a key pair
                            result = new KeyPair(publicKey, (PrivateKey) key);
                        }
                    } catch (UnrecoverableKeyException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return result;
                }
                
                @Override
                public PublicKey getFriendPublicKey(String user) {
                	PublicKey pk = null;
                	try {
                		char[] password = new char[] { 'b', 'l', 'o', 'g', 'r', 'a', 'c', 'y' };
                		InputStream is = getResourceAsStream("blogracy.jks");
                		try {
                			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                			keyStore.load(is, password);
						
                			java.security.cert.Certificate cert = keyStore.getCertificate(user);
						
                			// Get public key
                			pk = cert.getPublicKey();
			
                		} finally {
                			if(is != null) is.close();
                		}
                	} catch (NoSuchAlgorithmException e) {
                		e.printStackTrace();
                	} catch (KeyStoreException e) {
                		e.printStackTrace();
                	} catch (CertificateException e) {
                		e.printStackTrace();
                	} catch (IOException e) {
                		e.printStackTrace();
                	}
                return pk;
                }
            };
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
