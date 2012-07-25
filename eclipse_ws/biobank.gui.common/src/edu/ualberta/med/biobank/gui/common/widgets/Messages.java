package edu.ualberta.med.biobank.gui.common.widgets;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "edu.ualberta.med.biobank.gui.common.widgets.messages"; //$NON-NLS-1$
    public static String AbstractInfoTableWidget_add;
    public static String AbstractInfoTableWidget_delete;
    public static String AbstractInfoTableWidget_edit;
    public static String AbstractInfoTableWidget_first_label;
    public static String AbstractInfoTableWidget_last_label;
    public static String AbstractInfoTableWidget_load_error_title;
    public static String AbstractInfoTableWidget_next_label;
    public static String AbstractInfoTableWidget_previous_label;
    public static String BgcFileBrowser_browse;
    public static String BgcFileBrowser_open;
    public static String PaginationWidget_page_counter;
    public static String PaginationWidget_page_counter_unknown_total;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}