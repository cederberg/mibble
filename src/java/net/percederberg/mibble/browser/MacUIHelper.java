/*
 * MacUIHelper.java
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
 * Copyright (c) 2009 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import com.apple.eawt.Application;
import com.apple.eawt.ApplicationAdapter;
import com.apple.eawt.ApplicationEvent;

/**
 * Provides UI glue for the Mac OS X platform.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.9
 */
class MacUIHelper extends ApplicationAdapter {

    /**
     * The browser frame used.
     */
    private BrowserFrame frame;

    /**
     * Creates a new Mac OS UI helper.
     *
     * @param frame          the main application frame
     */
    public MacUIHelper(BrowserFrame frame) {
        Application app = Application.getApplication();

        this.frame = frame;
        app.addAboutMenuItem();
        app.removePreferencesMenuItem();
        app.addApplicationListener(this);
    }

    /**
     * Handles the about menu event.
     *
     * @param event          the application event
     */
    public void handleAbout(ApplicationEvent event) {
        event.setHandled(true);
        frame.showAbout();
    }

    /**
     * Handles the quit menu event.
     *
     * @param event          the application event
     */
    public void handleQuit(ApplicationEvent event) {
        event.setHandled(true);
        frame.quit();
    }
}
