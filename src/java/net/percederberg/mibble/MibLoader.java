/*
 * MibLoader.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * As a special exception, the copyright holders of this library give
 * you permission to link this library with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is
 * not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 *
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

/**
 * A MIB file loader. This class contains a search path for locating
 * MIB files, and also holds a refererence to previously loaded MIB 
 * files to avoid loading the same file multiple times. The MIB 
 * search path consists of directories with MIB files that can be 
 * imported into another MIB. If an import isn't found in the search
 * path, the default IANA and IETF MIB directories are searched. Note
 * that these files are normally stored as resources together with
 * the compiled code.<p>
 * 
 * The MIB loader is not thread-safe, i.e. it cannot be used 
 * concurrently in multiple threads. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class MibLoader {

    /**
     * The base resource directories. These are used when locating a
     * MIB stored as a resource inside the JAR file. 
     */
    private static final String[] RESOURCE_BASE = 
        { "mibs/iana", "mibs/ietf" };
    
    /**
     * The MIB file search path. This is a list of directories to 
     * search for MIB files. If a MIB isn't found among these 
     * directories, the default resource paths will be attempted.
     */
    private ArrayList dirs = new ArrayList();

    /**
     * The MIB files loaded. This list contains all MIB file loaded 
     * with this loader, in order to avoid loading some MIB files 
     * multiple times (and thus duplicating import symbols).
     */
    private ArrayList mibs = new ArrayList();

    /**
     * The queue of MIB files to load. This queue contains file 
     * objects.
     */
    private ArrayList queue = new ArrayList();

    /**
     * The default MIB context.
     */
    private DefaultContext context = new DefaultContext();

    /**
     * Creates a new MIB loader.
     */
    public MibLoader() {
    }

    /**
     * Adds a directory to the MIB search path. If the directory
     * specified is null, the current working directory will be added.
     * 
     * @param dir            the directory to add
     */
    public void addDir(File dir) {
        if (dir == null) {
            dir = new File(".");
        }
        if (!dirs.contains(dir)) {
            dirs.add(dir);
        }
    }
    
    /**
     * Adds directories to the MIB search path. 
     * 
     * @param dirs           the directories to add
     */
    public void addDirs(File[] dirs) {
        for (int i = 0; i < dirs.length; i++) {
            addDir(dirs[i]);
        }
    }

    /**
     * Adds a directory and all subdirectories to the MIB search path.
     * If the directory specified is null, the current working
     * directory (and subdirectories) will be added.
     * 
     * @param dir            the directory to add
     */
    public void addAllDirs(File dir) {
        File[]  files;

        if (dir == null) {
            dir = new File(".");
        }
        addDir(dir);
        files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addAllDirs(files[i]);
            }
        }
    }

    /**
     * Removes a directory from the MIB search path.
     * 
     * @param dir            the directory to remove
     */
    public void removeDir(File dir) {
        dirs.remove(dir);
    }

    /**
     * Removes all directories from the MIB search path.
     */
    public void removeAllDirs() {
        dirs.clear();
    }

    /**
     * Resets this loader. This means that all references to previuos
     * MIB files will be removed, forcing a reload of any imported 
     * MIB.
     */
    public void reset() {
        mibs.clear();
        queue.clear();
        context = new DefaultContext();
    }

    /**
     * Returns the default MIB context. This context contains the
     * symbols that are predefined for all MIB:s (such as 'iso').
     * 
     * @return the default MIB context
     */
    public MibContext getDefaultContext() {
        return context;
    }

    /**
     * Returns a previously loaded MIB file. If the MIB file hasn't
     * been loaded, null will be returned. The MIB is identified by
     * it's MIB name (i.e. the module name).
     * 
     * @param name           the MIB (module) name
     * 
     * @return the MIB module if found, or
     *         null otherwise
     */
    public Mib getMib(String name) {
        Mib  mib;

        for (int i = 0; i < mibs.size(); i++) {
            mib = (Mib) mibs.get(i);
            if (name.equals(mib.getName())) {
                return mib;
            }
        }
        return null;
    }
    
    /**
     * Returns all previously loaded MIB files. If no MIB files have
     * been loaded an empty array will be returned.
     * 
     * @return an array with all loaded MIB files
     * 
     * @since 2.2
     */
    public Mib[] getAllMibs() {
        Mib[]  res;
        
        res = new Mib[mibs.size()];
        mibs.toArray(res);
        return res;
    }

    /**
     * Searches for a MIB in the search path. The name specified
     * should be the MIB file name, possibly leaving out the 
     * extension. If the MIB isn't found in the directory search path,
     * the base resource path is also tested.
     * 
     * @param name           the MIB file name (without extension)
     * 
     * @return the MIB found, or 
     *         null if no MIB was found
     */
    private MibSource locate(String name) {
        File    dir;
        File[]  files;
        URL     url;
        int     i;

        for (i = 0; i < dirs.size(); i++) {
            dir = (File) dirs.get(i);
            files = dir.listFiles(new MibFileFilter(name));
            if (files != null && files.length > 0) {
                return new MibSource(files[0]);
            }
        }
        for (i = 0; i < RESOURCE_BASE.length; i++) {
            url = ClassLoader.getSystemResource(RESOURCE_BASE[i] + "/" + name);
            if (url != null) {
                return new MibSource(name, url);
            }
        }
        return null;
    }

    /**
     * Loads a MIB file. The file is searched for in the MIB search
     * path. The MIB is identified by it's MIB name (i.e. the module 
     * name). This method will also load all imported MIB:s if not 
     * previously loaded by this loader.
     * 
     * @param name           the MIB name (filename without extension)
     * 
     * @return the MIB file loaded
     * 
     * @throws FileNotFoundException if the MIB file couldn't be 
     *             found in the MIB search path
     * @throws MibLoaderException if the MIB file couldn't be loaded 
     *             correctly
     */
    public Mib load(String name)
        throws FileNotFoundException, MibLoaderException {

        MibSource  src;
        
        src = locate(name);
        if (src == null) {
            throw new FileNotFoundException("couldn't locate MIB: '" + 
                                            name + "'");
        }
        return load(src);
    }
    
    /**
     * Loads a MIB file. This method will also load all imported 
     * MIB:s if not previously loaded by this loader.
     * 
     * @param file           the MIB file
     * 
     * @return the MIB file loaded
     * 
     * @throws FileNotFoundException if the MIB file couldn't be 
     *             found
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     */
    public Mib load(File file) 
        throws FileNotFoundException, MibLoaderException {

        return load(new MibSource(file));
    }

    /**
     * Loads a MIB. This method will also load all imported MIB:s if
     * not previously loaded by this loader.
     * 
     * @param src            the MIB source
     * 
     * @return the MIB loaded
     * 
     * @throws FileNotFoundException if the MIB couldn't be found
     * @throws MibLoaderException if the MIB couldn't be loaded 
     *             correctly
     */
    private Mib load(MibSource src)
        throws FileNotFoundException, MibLoaderException {

        int           position;
        MibLoaderLog  log;

        position = mibs.size();
        queue.clear();
        queue.add(src);
        log = loadQueue();
        if (log.errorCount() > 0) {
            throw new MibLoaderException(log);
        }
        return (Mib) mibs.get(position);
    }

    /**
     * Schedules the loading of a MIB file. The file is added to the
     * queue of MIB files to be loaded, unless it is already loaded 
     * or in the queue. The file is searched for in the MIB search
     * path.
     * 
     * @param name           the MIB name (filename without extension)
     * 
     * @throws FileNotFoundException if the MIB file couldn't be 
     *             found in the MIB search path
     */
    void scheduleLoad(String name) throws FileNotFoundException {
        MibSource  src;
        
        src = locate(name);
        if (src == null) {
            throw new FileNotFoundException("couldn't locate MIB: '" + 
                                            name + "'");
        }
        scheduleLoad(src);
    }

    /**
     * Schedules the loading of a MIB file. The file is added to the
     * queue of MIB files to be loaded, unless it is already loaded 
     * or in the queue.
     * 
     * @param file           the MIB file
     */
    void scheduleLoad(File file) {
        scheduleLoad(new MibSource(file));
    }

    /**
     * Schedules the loading of a MIB. The MIB source is added to the
     * queue of MIB:s to be loaded, unless it is already loaded or in
     * the queue.
     * 
     * @param src            the MIB source
     */
    void scheduleLoad(MibSource src) {
        if (!mibs.contains(src.getFile()) && !queue.contains(src)) {
            queue.add(src);
        }
    }

    /**
     * Loads all MIB files in the loader queue. New entries may be
     * added to the queue while loading a MIB, as a result of 
     * importing other MIB files. This method will either load all
     * MIB files in the queue or none (if errors were encountered).
     * 
     * @return the loader log for the whole queue
     * 
     * @throws FileNotFoundException if the MIB file in the queue 
     *             couldn't be found
     */
    private MibLoaderLog loadQueue() throws FileNotFoundException {
        MibLoaderLog  log = new MibLoaderLog();
        ArrayList     processed = new ArrayList();
        MibSource     src;

        // Parse MIB files in queue
        while (queue.size() > 0) {
            try {
                src = (MibSource) queue.get(0);
                processed.add(src.createMib(this, log));
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log 
            }
            queue.remove(0);
        }
        mibs.addAll(processed);

        // Initialize all parsed MIB files
        for (int i = 0; i < processed.size(); i++) {
            try {
                ((Mib) processed.get(i)).initialize();
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log 
            }
        }

        // Validate all parsed MIB files
        for (int i = 0; i < processed.size(); i++) {
            try {
                ((Mib) processed.get(i)).validate();
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log 
            }
        }

        // Handle errors
        if (log.errorCount() > 0) {
            mibs.removeAll(processed);
        }
        
        return log;
    }


    /**
     * A MIB input source. This class encapsulates the two different 
     * ways of loacating a MIB file, either through a file or a URL.
     */
    private class MibSource {
        
        /**
         * The MIB file. This variable must always be set, as it is
         * used for printing the MIB name.
         */
        private File file = null;
        
        /**
         * The MIB URL location. This variable is only set if the MIB
         * is read from a URL.
         */
        private URL url = null;
        
        /**
         * Creates a new MIB input source. The MIB will be read from
         * the specified file.
         * 
         * @param file           the file to read from
         */
        public MibSource(File file) {
            this.file = file;
        }
        
        /**
         * Creates a new MIB input source. The MIB will be read from
         * the specified URL.
         * 
         * @param name           the MIB name
         * @param url            the URL to read from
         */
        public MibSource(String name, URL url) {
            this.file = new File(name);
            this.url = url;
        }
        
        /**
         * Checks if this object is equal to another. This method 
         * will only return true for another mib source object with
         * the same input source.
         * 
         * @param obj            the object to compare with
         * 
         * @return true if the object is equal to this, or
         *         false otherwise
         */
        public boolean equals(Object obj) {
            MibSource  src;

            if (obj instanceof MibSource) {
                src = (MibSource) obj;
                if (url == null) {
                    return file.equals(src.file);
                } else {
                    return url.equals(src.url);
                }
            } else {
                return false;
            }
        }

        /**
         * Returns the MIB file. If the MIB is loaded from URL this
         * file does not actually exist, but is used for providing a 
         * unique reference to the MIB.
         * 
         * @return the MIB file
         */
        public File getFile() {
            return file;
        }

        /**
         * Creates the MIB container. This method will read the MIB
         * either from file or from URL.
         * 
         * @param loader         the MIB loader to use for imports
         * @param log            the MIB log to use for errors
         * 
         * @return the MIB container created
         * 
         * @throws FileNotFoundException if the MIB couldn't be found
         * @throws MibLoaderException if the MIB couldn't be parsed 
         *             or analyzed correctly
         */
        public Mib createMib(MibLoader loader, MibLoaderLog log) 
            throws FileNotFoundException, MibLoaderException {

            Reader  input;

            if (url == null) {
                return new Mib(file, loader, log);
            } else {
                try {
                    input = new InputStreamReader(url.openStream());
                    return new Mib(input, file, loader, log);
                } catch (IOException e) {
                    throw new FileNotFoundException("couldn't find '" +
                                                    file + "' MIB");
                }
            }
        }
    }

    /**
     * A MIB file name filter. This filter compares a file name with
     * a MIB name, accepting only files with the MIB name. Any 
     * extension on the file will be disregarded, but the name 
     * comparison is made in a case-sensitive way. 
     */
    private class MibFileFilter implements FilenameFilter {
    
        /**
         * The base MIB name.
         */
        private String basename;
        
        /**
         * Creates a new MIB file filter.
         * 
         * @param name           the MIB name to filter with
         */
        public MibFileFilter(String name) {
            this.basename = name;
        }

        /**
         * Checks if a file name matches the MIB name.
         * 
         * @param dir            the file directory
         * @param name           the file name
         * 
         * @return true if the file name matches, or 
         *         false otherwise
         */
        public boolean accept(File dir, String name) {
            return name.equals(basename) 
                || name.startsWith(basename + ".");
        }
    }    
}
