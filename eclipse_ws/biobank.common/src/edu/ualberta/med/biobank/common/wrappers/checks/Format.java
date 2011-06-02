package edu.ualberta.med.biobank.common.wrappers.checks;

import java.util.Collection;

import edu.ualberta.med.biobank.common.wrappers.Property;

class Format {
    private static final String DELIMITER = ", ";

    public static String modelClass(Class<?> modelClass) {
        // TODO: some formatting? language translation lookup?
        return modelClass.getSimpleName();
    }

    public static <E> String propertyValues(E model,
        Collection<Property<?, ? super E>> properties) {
        StringBuilder sb = new StringBuilder();
        int n = properties.size();
        int i = 0;
        for (Property<?, ? super E> property : properties) {
            sb.append(property.get(model).toString());
            i++;
            if (i < n) {
                sb.append(DELIMITER);
            }
        }
        return sb.toString();
    }

    public static <E> String propertyNames(
        Collection<Property<?, ? super E>> properties) {
        StringBuilder sb = new StringBuilder();
        int n = properties.size();
        int i = 0;
        for (Property<?, ? super E> property : properties) {
            sb.append(propertyName(property));
            i++;
            if (i < n) {
                sb.append(DELIMITER);
            }
        }
        return sb.toString();
    }

    public static String propertyName(Property<?, ?> property) {
        // TODO: some formatting? language translation lookup?
        // TODO: what about things like address.city? lookup probably best.
        return property.getName();
    }
}