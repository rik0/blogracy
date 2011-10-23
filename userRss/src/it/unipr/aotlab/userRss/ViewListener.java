package it.unipr.aotlab.userRss;

import org.eclipse.swt.widgets.Composite;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEvent;
import org.gudy.azureus2.ui.swt.plugins.UISWTViewEventListener;

/**
 * Created by IntelliJ IDEA.
 * User: enrico
 * Date: 10/23/11
 * Time: 6:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewListener implements UISWTViewEventListener {


    @Override
    public boolean eventOccurred(UISWTViewEvent uiswtViewEvent) {
        UserRSS.logError("4: " + uiswtViewEvent.getType());
        switch (uiswtViewEvent.getType()) {
            case UISWTViewEvent.TYPE_CREATE:
                return View.shouldCreateView();
            case UISWTViewEvent.TYPE_DESTROY:
                View.destroyView();
                return true;
            case UISWTViewEvent.TYPE_INITIALIZE:
                Composite composite = (Composite)uiswtViewEvent.getData();
                return View.createView(composite);
            case UISWTViewEvent.TYPE_LANGUAGEUPDATE:
                View theView = View.getView();
                theView.changeLanguage();
                return true;
            case UISWTViewEvent.TYPE_REFRESH:
                return true;
        }

        return true;
    }
}
