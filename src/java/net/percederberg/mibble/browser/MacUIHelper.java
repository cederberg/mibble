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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Provides UI glue for the Mac OS X platform. This class uses Java
 * reflection in order to be compilable everywhere.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.9
 */
class MacUIHelper implements InvocationHandler {

    /**
     * A constant flag that is set to true if this application runs
     * on Mac OS X.
     */
    public static final boolean IS_MAC_OS =
        System.getProperty("os.name").toLowerCase().startsWith("mac os x");

    /**
     * The browser frame used.
     */
    private BrowserFrame frame;

    /**
     * Creates a new Mac OS UI helper. This constructor should NOT be
     * called on other operating environments, since it loads and uses
     * Apple-specific classes through Java reflection.
     *
     * @param frame          the main application frame
     */
    public MacUIHelper(BrowserFrame frame) {
        ClassLoader  cl = getClass().getClassLoader();
        Class        appCls;
        Class        lstnCls;
        Object       app;
        Method       method;
        Object       proxy;

        this.frame = frame;
        try {
            appCls = Class.forName("com.apple.eawt.Application");
            lstnCls = Class.forName("com.apple.eawt.ApplicationListener");
            method = appCls.getMethod("getApplication", new Class[] {});
            app = method.invoke(null, new Object[] {});
            method = appCls.getMethod("addAboutMenuItem", new Class[] {});
            method.invoke(app, new Object[] {});
            method = appCls.getMethod("removePreferencesMenuItem",
                                      new Class[] {});
            method.invoke(app, new Object[] {});
            method = appCls.getMethod("addApplicationListener",
                                      new Class[] { lstnCls });
            proxy = Proxy.newProxyInstance(cl, new Class[] { lstnCls }, this);
            method.invoke(app, new Object[] { proxy });
        } catch (Exception e) {
            System.err.println("Failed to initialize Mac OS Application:");
            e.printStackTrace();
        }
    }

    /**
     * Handles calls on the com.apple.eawt.ApplicationListener
     * interface.
     *
     * @param p              the proxy object
     * @param m              the method being called
     * @param args           the call arguments
     *
     * @return the call response
     */
    public Object invoke(Object p, Method m, Object[] args) throws Throwable {
        if (m.getName().equals("handleAbout")) {
            setHandled(args[0]);
            frame.showAbout();
        } else if (m.getName().equals("handleQuit")) {
            setHandled(args[0]);
            frame.quit();
        }
        return null;
    }

    /**
     * Calls the setHandled(true) method on the specified event.
     *
     * @param event          the event object instance
     */
    private void setHandled(Object event) {
        Class   cls = event.getClass();
        Method  method;

        try {
            method = cls.getMethod("setHandled", new Class[] { boolean.class });
            method.invoke(event, new Object[] { Boolean.TRUE });
        } catch (Exception e) {
            System.err.println("Failed to invoke ApplicationEvent.setHandled(true):");
            e.printStackTrace();
        }
    }
}
