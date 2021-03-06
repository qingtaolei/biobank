package edu.ualberta.med.biobank.widgets.utils;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.Section;

import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.widgets.utils.BgcWidgetCreator;

public class WidgetCreator extends BgcWidgetCreator {

    public WidgetCreator(Map<String, Control> controls) {
        super(controls);
    }

    public void addSectionToolbar(Section section, String tooltip,
        SelectionListener listener, BgcPlugin.Image image) {
        ToolBar tbar = (ToolBar) section.getTextClient();
        if (tbar == null) {
            tbar = new ToolBar(section, SWT.FLAT | SWT.HORIZONTAL);
            section.setTextClient(tbar);
        }

        ToolItem titem = new ToolItem(tbar, SWT.NULL);
        if (image == null) {
            image = BgcPlugin.Image.ADD;
        }
        titem.setImage(BgcPlugin.getDefault().getImage(image));
        titem.setToolTipText(tooltip);
        titem.addSelectionListener(listener);
    }
}
