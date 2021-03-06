/*
 * Copyright (C) 2016 Blue Jay Wireless
 * Copyright (C) 2015-2016 Daniel Martí <mvdan@mvdan.cc>
 * Copyright (C) 2014-2016 Hans-Christoph Steiner <hans@eds.org>
 * Copyright (C) 2014-2016 Peter Serwylo <peter@serwylo.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */

package org.fdroid.fdroid;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.ApkProvider;
import org.fdroid.fdroid.data.App;
import org.fdroid.fdroid.data.AppProvider;
import org.fdroid.fdroid.data.Repo;
import org.fdroid.fdroid.data.RepoPersister;
import org.fdroid.fdroid.data.RepoProvider;
import org.fdroid.fdroid.data.RepoPushRequest;
import org.fdroid.fdroid.data.Schema.RepoTable;
import org.fdroid.fdroid.installer.InstallManagerService;
import org.fdroid.fdroid.installer.InstallerService;
import org.fdroid.fdroid.net.Downloader;
import org.fdroid.fdroid.net.DownloaderFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Updates the local database with a repository's app/apk metadata and verifying
 * the JAR signature on the file received from the repository. As an overview:
 * <ul>
 * <li>Download the {@code index.jar}
 * <li>Verify that it is signed correctly and by the correct certificate
 * <li>Parse the {@code index.xml} that is in {@code index.jar}
 * <li>Save the resulting repo, apps, and apks to the database.
 * <li>Process any push install/uninstall requests included in the repository
 * </ul>
 * <b>WARNING</b>: this class is the central piece of the entire security model of
 * FDroid!  Avoid modifying it when possible, if you absolutely must, be very,
 * very careful with the changes that you are making!
 */
public class RepoUpdater {

    private static final String TAG = "RepoUpdater";

    private final String indexUrl;

    @NonNull
    private final Context context;
    @NonNull
    private final Repo repo;
    private boolean hasChanged;

    @Nullable
    private ProgressListener downloadProgressListener_applist; // sam
    private ProgressListener downloadProgressListener;
    private ProgressListener committingProgressListener;
    private ProgressListener processXmlProgressListener;
    private String cacheTag;
    private String cacheTag_applist; // sam
    private X509Certificate signingCertFromJar;

    @NonNull
    private final RepoPersister persister;

    private final List<RepoPushRequest> repoPushRequestList = new ArrayList<>();

    public static void getAllowedAppsFromFile (File fl,List appList) throws Exception
    {
        FileInputStream fin = new FileInputStream(fl);

        BufferedReader reader = new BufferedReader(new InputStreamReader(fin));
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            if ( !line.startsWith("#") )
            {
                appList.add(line);
                Log.w(TAG, "Parsing File:" + line);
            }
        }
        reader.close();

        //String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
    }

    /**
     * Updates an app repo as read out of the database into a {@link Repo} instance.
     *
     * @param repo A {@link Repo} read out of the local database
     */
    public RepoUpdater(@NonNull Context context, @NonNull Repo repo) {
        this.context = context;
        this.repo = repo;
        this.persister = new RepoPersister(context, repo);

        String url = repo.address + "/index.jar";
        String versionName = Utils.getVersionName(context);
        if (versionName != null) {
            url += "?client_version=" + versionName;
        }
        this.indexUrl = url;
    }

    public void setDownloadProgressListener(ProgressListener progressListener) {
        this.downloadProgressListener = progressListener;
    }

    public void setProcessXmlProgressListener(ProgressListener progressListener) {
        this.processXmlProgressListener = progressListener;
    }

    public void setCommittingProgressListener(ProgressListener progressListener) {
        this.committingProgressListener = progressListener;
    }

    public boolean hasChanged() {
        return hasChanged;
    }

    // sam
    // ToDo: optimize this later to check the tag of the applist file and if it is not changed not do anything
    private Downloader downloadAppList() throws UpdateException {
        Downloader downloader = null;

        try {

            String url = Preferences.get().getAllowedAppsURL();

            if (url == null)
                throw new UpdateException(repo, "Error getting user application list, have you signed-in?", new IOException());

            downloader = DownloaderFactory.create(context, url);
            //downloader.setCacheTag(repo.lastetag);
            downloader.setListener(downloadProgressListener_applist);
            downloader.download();

            //if (downloader.isCached()) {
            //    // The applist is unchanged since we last read it. We just mark
            //    // everything that came from this repo as being updated.
            //    Utils.debugLog(TAG, "App list is up to date (by etag)");
            //}

        } catch (IOException e) {
            if (downloader != null && downloader.outputFile != null) {
                if (!downloader.outputFile.delete()) {
                    Log.w(TAG, "Couldn't delete file: " + downloader.outputFile.getAbsolutePath());
                }
            }

            throw new UpdateException(repo, "Error getting applist file", e);
        } catch (InterruptedException e) {
            // ignored if canceled, the local database just won't be updated
            e.printStackTrace();
        }

        return downloader;
    }

    private Downloader downloadIndex() throws UpdateException {
        Downloader downloader = null;
        try {
            downloader = DownloaderFactory.create(context, indexUrl);
            downloader.setCacheTag(""/*repo.lastetag*/); // sam: force redownload always
            downloader.setListener(downloadProgressListener);
            downloader.download();

            if (downloader.isCached()) {
                // The index is unchanged since we last read it. We just mark
                // everything that came from this repo as being updated.
                Utils.debugLog(TAG, "Repo index for " + indexUrl + " is up to date (by etag)");
            }

        } catch (IOException e) {
            if (downloader != null && downloader.outputFile != null) {
                if (!downloader.outputFile.delete()) {
                    Log.w(TAG, "Couldn't delete file: " + downloader.outputFile.getAbsolutePath());
                }
            }

            throw new UpdateException(repo, "Error getting index file", e);
        } catch (InterruptedException e) {
            // ignored if canceled, the local database just won't be updated
            e.printStackTrace();
        }
        return downloader;
    }

    /**
     * All repos are represented by a signed jar file, {@code index.jar}, which contains
     * a single file, {@code index.xml}.  This takes the {@code index.jar}, verifies the
     * signature, then returns the unzipped {@code index.xml}.
     *
     * @throws UpdateException All error states will come from here.
     */
    // Sam
    private void setRefreshViewFlag()
    {
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor e = p.edit();
        e.putBoolean("refreshViewFlag", true);
        e.apply();
    }
    public void update() throws UpdateException {

        // sam
        Utils.debugLog(TAG, "RepoUpdater Update is called!");
        setRefreshViewFlag();

        //
        final Downloader downloader = downloadIndex();
        final Downloader downloader_applist = downloadAppList();


        // ToDo: currently we always consider that the applist has changed, optimize this by really doing this check
        // hasChanged = downloader.hasChanged() & downloader_applist.hasChanged();
        hasChanged = true;

        if (hasChanged) {

            List allowedApps = Preferences.get().getAllowedApps();

            try
            {
                allowedApps.clear();
                Utils.debugLog(TAG, "Clearing AllowedApps");
                getAllowedAppsFromFile(downloader_applist.outputFile, allowedApps);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            // Don't worry about checking the status code for 200. If it was a
            // successful download, then we will have a file ready to use:
            cacheTag = downloader.getCacheTag();
            processDownloadedFile(downloader.outputFile);
            processRepoPushRequests();
        }
    }

    private ContentValues repoDetailsToSave;
    private String signingCertFromIndexXml;

    private RepoXMLHandler.IndexReceiver createIndexReceiver() {
        return new RepoXMLHandler.IndexReceiver() {
            @Override
            public void receiveRepo(String name, String description, String signingCert, int maxAge, int version, long timestamp) {
                signingCertFromIndexXml = signingCert;
                repoDetailsToSave = prepareRepoDetailsForSaving(name, description, maxAge, version, timestamp);
            }

            @Override
            public void receiveApp(App app, List<Apk> packages) {
                try {

                    // Sam here we check and ignore stuff
                    //AppFilter filter = new AppFilter();

                    //if ( !filter.exclude(app) ) // Sam
                    persister.saveToDb(app, packages);
                } catch (UpdateException e) {
                    throw new RuntimeException("Error while saving repo details to database.", e);
                }
            }

            @Override
            public void receiveRepoPushRequest(RepoPushRequest repoPushRequest) {
                repoPushRequestList.add(repoPushRequest);
            }
        };
    }

    public void processDownloadedFile(File downloadedFile) throws UpdateException {
        InputStream indexInputStream = null;
        try {
            if (downloadedFile == null || !downloadedFile.exists()) {
                throw new UpdateException(repo, downloadedFile + " does not exist!");
            }

            // Due to a bug in Android 5.0 Lollipop, the inclusion of spongycastle causes
            // breakage when verifying the signature of the downloaded .jar. For more
            // details, check out https://gitlab.com/fdroid/fdroidclient/issues/111.
            FDroidApp.disableSpongyCastleOnLollipop();

            JarFile jarFile = new JarFile(downloadedFile, true);
            JarEntry indexEntry = (JarEntry) jarFile.getEntry("index.xml");
            indexInputStream = new ProgressBufferedInputStream(jarFile.getInputStream(indexEntry),
                    processXmlProgressListener, new URL(repo.address), (int) indexEntry.getSize());

            // Process the index...
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            final SAXParser parser = factory.newSAXParser();
            final XMLReader reader = parser.getXMLReader();
            final RepoXMLHandler repoXMLHandler = new RepoXMLHandler(repo, createIndexReceiver());
            reader.setContentHandler(repoXMLHandler);
            reader.parse(new InputSource(indexInputStream));

            long timestamp = repoDetailsToSave.getAsLong(RepoTable.Cols.TIMESTAMP);
            if (timestamp < repo.timestamp) {
                throw new UpdateException(repo, "index.jar is older that current index! "
                        + timestamp + " < " + repo.timestamp);
            }

            signingCertFromJar = getSigningCertFromJar(indexEntry);

            // JarEntry can only read certificates after the file represented by that JarEntry
            // has been read completely, so verification cannot run until now...
            assertSigningCertFromXmlCorrect();
            commitToDb();
        } catch (SAXException | ParserConfigurationException | IOException e) {
            throw new UpdateException(repo, "Error parsing index", e);
        } finally {
            FDroidApp.enableSpongyCastleOnLollipop();
            Utils.closeQuietly(indexInputStream);
            if (downloadedFile != null) {
                if (!downloadedFile.delete()) {
                    Log.w(TAG, "Couldn't delete file: " + downloadedFile.getAbsolutePath());
                }
            }
        }
    }

    private void commitToDb() throws UpdateException {
        Log.i(TAG, "Repo signature verified, saving app metadata to database.");
        if (committingProgressListener != null) {
            try {
                //TODO this should be an event, not a progress listener
                committingProgressListener.onProgress(new URL(indexUrl), 0, -1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        persister.commit(repoDetailsToSave);
    }

    private void assertSigningCertFromXmlCorrect() throws SigningException {

        // no signing cert read from database, this is the first use
        if (repo.signingCertificate == null) {
            verifyAndStoreTOFUCerts(signingCertFromIndexXml, signingCertFromJar);
        }
        verifyCerts(signingCertFromIndexXml, signingCertFromJar);

    }

    /**
     * Update tracking data for the repo represented by this instance (index version, etag,
     * description, human-readable name, etc.
     */
    private ContentValues prepareRepoDetailsForSaving(String name, String description, int maxAge, int version, long timestamp) {
        ContentValues values = new ContentValues();

        values.put(RepoTable.Cols.LAST_UPDATED, Utils.formatTime(new Date(), ""));

        if (repo.lastetag == null || !repo.lastetag.equals(cacheTag)) {
            values.put(RepoTable.Cols.LAST_ETAG, cacheTag);
        }

        if (version != -1 && version != repo.version) {
            Utils.debugLog(TAG, "Repo specified a new version: from " + repo.version + " to " + version);
            values.put(RepoTable.Cols.VERSION, version);
        }

        if (maxAge != -1 && maxAge != repo.maxage) {
            Utils.debugLog(TAG, "Repo specified a new maximum age - updated");
            values.put(RepoTable.Cols.MAX_AGE, maxAge);
        }

        if (description != null && !description.equals(repo.description)) {
            values.put(RepoTable.Cols.DESCRIPTION, description);
        }

        if (name != null && !name.equals(repo.name)) {
            values.put(RepoTable.Cols.NAME, name);
        }

        // Always put a timestamp here, even if it is the same. This is because we are dependent
        // on it later on in the process. Specifically, when updating from a HTTP server that
        // doesn't send out etags with its responses, it will trigger a full blown repo update
        // every time, even if all the values in the index are the same (name, description, etc).
        // In such a case, the remainder of the update process will proceed, and ask for this
        // timestamp.
        values.put(RepoTable.Cols.TIMESTAMP, timestamp);

        return values;
    }

    public static class UpdateException extends Exception {

        private static final long serialVersionUID = -4492452418826132803L;
        public final Repo repo;

        public UpdateException(Repo repo, String message) {
            super(message);
            this.repo = repo;
        }

        public UpdateException(Repo repo, String message, Exception cause) {
            super(message, cause);
            this.repo = repo;
        }
    }

    public static class SigningException extends UpdateException {
        public SigningException(Repo repo, String message) {
            super(repo, "Repository was not signed correctly: " + message);
        }
    }

    /**
     * FDroid's index.jar is signed using a particular format and does not allow lots of
     * signing setups that would be valid for a regular jar.  This validates those
     * restrictions.
     */
    private X509Certificate getSigningCertFromJar(JarEntry jarEntry) throws SigningException {
        final CodeSigner[] codeSigners = jarEntry.getCodeSigners();
        if (codeSigners == null || codeSigners.length == 0) {
            throw new SigningException(repo, "No signature found in index");
        }
        /* we could in theory support more than 1, but as of now we do not */
        if (codeSigners.length > 1) {
            throw new SigningException(repo, "index.jar must be signed by a single code signer!");
        }
        List<? extends Certificate> certs = codeSigners[0].getSignerCertPath().getCertificates();
        if (certs.size() != 1) {
            throw new SigningException(repo, "index.jar code signers must only have a single certificate!");
        }
        return (X509Certificate) certs.get(0);
    }

    /**
     * A new repo can be added with or without the fingerprint of the signing
     * certificate.  If no fingerprint is supplied, then do a pure TOFU and just
     * store the certificate as valid.  If there is a fingerprint, then first
     * check that the signing certificate in the jar matches that fingerprint.
     */
    private void verifyAndStoreTOFUCerts(String certFromIndexXml, X509Certificate rawCertFromJar)
            throws SigningException {
        if (repo.signingCertificate != null) {
            return; // there is a repo.signingCertificate already, nothing to TOFU
        }

        /* The first time a repo is added, it can be added with the signing certificate's
         * fingerprint.  In that case, check that fingerprint against what is
         * actually in the index.jar itself.  If no fingerprint, just store the
         * signing certificate */
        if (repo.fingerprint != null) {
            String fingerprintFromIndexXml = Utils.calcFingerprint(certFromIndexXml);
            String fingerprintFromJar = Utils.calcFingerprint(rawCertFromJar);
            if (!repo.fingerprint.equalsIgnoreCase(fingerprintFromIndexXml)
                    || !repo.fingerprint.equalsIgnoreCase(fingerprintFromJar)) {
                throw new SigningException(repo, "Supplied certificate fingerprint does not match!");
            }
        } // else - no info to check things are valid, so just Trust On First Use

        Utils.debugLog(TAG, "Saving new signing certificate in the database for " + repo.address);
        ContentValues values = new ContentValues(2);
        values.put(RepoTable.Cols.LAST_UPDATED, Utils.formatDate(new Date(), ""));
        values.put(RepoTable.Cols.SIGNING_CERT, Hasher.hex(rawCertFromJar));
        RepoProvider.Helper.update(context, repo, values);
    }

    /**
     * FDroid works with three copies of the signing certificate:
     * <li>in the downloaded jar</li>
     * <li>in the index XML</li>
     * <li>stored in the local database</li>
     * It would work better removing the copy from the index XML, but it needs to stay
     * there for backwards compatibility since the old TOFU process requires it.  Therefore,
     * since all three have to be present, all three are compared.
     *
     * @param certFromIndexXml the cert written into the header of the index XML
     * @param rawCertFromJar   the {@link X509Certificate} embedded in the downloaded jar
     */
    private void verifyCerts(String certFromIndexXml, X509Certificate rawCertFromJar) throws SigningException {
        // convert binary data to string version that is used in FDroid's database
        String certFromJar = Hasher.hex(rawCertFromJar);

        // repo and repo.signingCertificate must be pre-loaded from the database
        if (TextUtils.isEmpty(repo.signingCertificate)
                || TextUtils.isEmpty(certFromJar)
                || TextUtils.isEmpty(certFromIndexXml)) {
            throw new SigningException(repo, "A empty repo or signing certificate is invalid!");
        }

        // though its called repo.signingCertificate, its actually a X509 certificate
        if (repo.signingCertificate.equals(certFromJar)
                && repo.signingCertificate.equals(certFromIndexXml)
                && certFromIndexXml.equals(certFromJar)) {
            return; // we have a match!
        }
        throw new SigningException(repo, "Signing certificate does not match!");
    }

    /**
     * Server index XML can include optional {@code install} and {@code uninstall}
     * requests.  This processes those requests, figuring out whether the client
     * should always accept, prompt the user, or ignore those requests on a
     * per repo basis.
     */
    private void processRepoPushRequests() {
        PackageManager pm = context.getPackageManager();

        for (RepoPushRequest repoPushRequest : repoPushRequestList) {
            String packageName = repoPushRequest.packageName;
            PackageInfo packageInfo = null;
            try {
                packageInfo = pm.getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                // ignored
            }
            if (RepoPushRequest.INSTALL.equals(repoPushRequest.request)) {
                ContentResolver cr = context.getContentResolver();

                // TODO: In the future, this needs to be able to specify which repository to get
                // the package from. Better yet, we should be able to specify the hash of a package
                // to install (especially when we move to using hashes more as identifiers than we
                // do righ tnow).
                App app = AppProvider.Helper.findHighestPriorityMetadata(cr, packageName);
                if (app == null) {
                    Utils.debugLog(TAG, packageName + " not in local database, ignoring request to"
                            + repoPushRequest.request);
                    continue;
                }
                int versionCode;
                if (repoPushRequest.versionCode == null) {
                    versionCode = app.suggestedVersionCode;
                } else {
                    versionCode = repoPushRequest.versionCode;
                }
                if (packageInfo != null && versionCode == packageInfo.versionCode) {
                    Utils.debugLog(TAG, repoPushRequest + " already installed, ignoring");
                } else {
                    Apk apk = ApkProvider.Helper.findApkFromAnyRepo(context, packageName, versionCode);
                    InstallManagerService.queue(context, app, apk);
                }
            } else if (RepoPushRequest.UNINSTALL.equals(repoPushRequest.request)) {
                if (packageInfo == null) {
                    Utils.debugLog(TAG, "ignoring request, not installed: " + repoPushRequest);
                    continue;
                }
                if (repoPushRequest.versionCode == null
                        || repoPushRequest.versionCode == packageInfo.versionCode) {
                    Apk apk = ApkProvider.Helper.findApkFromAnyRepo(context, repoPushRequest.packageName,
                            packageInfo.versionCode);
                    InstallerService.uninstall(context, apk);
                } else {
                    Utils.debugLog(TAG, "ignoring request based on versionCode:" + repoPushRequest);
                }
            } else {
                Utils.debugLog(TAG, "Unknown Repo Push Request: " + repoPushRequest.request);
            }
        }
    }
}
