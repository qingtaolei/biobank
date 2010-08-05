package edu.ualberta.med.biobank.forms.reports;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import edu.ualberta.med.biobank.widgets.DateTimeWidget;
import edu.ualberta.med.biobank.widgets.TopContainerListWidget;

public class AliquotsByStudyClinicEditor extends ReportsEditor {

    public static String ID = "edu.ualberta.med.biobank.editors.AliquotsByStudyClinicEditor";
    protected DateTimeWidget start;
    protected DateTimeWidget end;
    protected TopContainerListWidget topContainers;

    @Override
    protected int[] getColumnWidths() {
        return new int[] { 100, 100, 100 };
    }

    @Override
    protected void createOptionSection(Composite parent) {
        widgetCreator.createLabel(parent, "Top Containers");
        topContainers = new TopContainerListWidget(parent, SWT.NONE);
        start = widgetCreator.createDateTimeWidget(parent,
            "Start Date (Linked)", null, null, null);
        end = widgetCreator.createDateTimeWidget(parent, "End Date (Linked)",
            null, null, null);
    }

    @Override
    protected List<Object> getParams() {
        List<Object> params = new ArrayList<Object>();
        params.add(topContainers.getSelectedContainers());
        if (start.getDate() == null)
            params.add(new Date(0));
        else
            params.add(start.getDate());
        if (end.getDate() == null)
            params.add(new Date());
        else
            params.add(end.getDate());
        return params;
    }

    @Override
    protected String[] getColumnNames() {
        return new String[] { "Study", "Clinic", "Total" };
    }

    @Override
    protected List<String> getParamNames() {
        List<String> paramNames = new ArrayList<String>();
        paramNames.add("Top Containers");
        paramNames.add("Start Date (Linked)");
        paramNames.add("End Date (Linked)");
        return paramNames;
    }

}
