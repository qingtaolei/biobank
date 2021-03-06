package edu.ualberta.med.biobank.mvp.view.item;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import edu.ualberta.med.biobank.mvp.user.ui.ValueField;

public class AdaptedValueField<T, U> extends AbstractValueField<T> {
    private final AdapteeMonitor adapteeMonitor = new AdapteeMonitor();
    private final ValueField<U> adaptee;
    private final Adapter<T, U> adapter;

    public AdaptedValueField(ValueField<U> adaptee, Adapter<T, U> adapter) {
        this.adaptee = adaptee;
        this.adapter = adapter;

        adaptee.addValueChangeHandler(adapteeMonitor);
    }

    @Override
    protected void updateGui() {
        T adaptedValue = getValue();
        U unadaptedValue = adapter.unadapt(adaptedValue);
        adaptee.setValue(unadaptedValue, false);
    }

    private class AdapteeMonitor implements ValueChangeHandler<U> {
        @Override
        public void onValueChange(ValueChangeEvent<U> event) {
            U unadaptedValue = adaptee.getValue();
            T adaptedValue = adapter.adapt(unadaptedValue);
            setValue(adaptedValue, true);
        }

    }
}
