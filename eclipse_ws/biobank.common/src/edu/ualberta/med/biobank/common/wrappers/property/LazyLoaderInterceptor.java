package edu.ualberta.med.biobank.common.wrappers.property;

import java.text.MessageFormat;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.ualberta.med.biobank.common.wrappers.Property;

public class LazyLoaderInterceptor implements GetterInterceptor {
    private static final String HQL = "SELECT o.{0} FROM {1} o WHERE o = ?";

    private final Session session;
    private final Integer maxMemoryDepth;
    private Integer depth = 1;

    public LazyLoaderInterceptor(Session session) {
        this(session, null);
    }

    public LazyLoaderInterceptor(Session session, Integer maxMemoryDepth) {
        this.session = session;
        this.maxMemoryDepth = maxMemoryDepth;
    }

    @Override
    public <P, M> P get(Property<P, M> subProperty, M model) {
        P value = null;
        String propertyName = subProperty.getName();

        if (isLegalDepth()
            && Hibernate.isPropertyInitialized(model, propertyName)) {
            value = subProperty.get(model);
        } else {
            String modelName = subProperty.getModelClass().getName();
            String hql = MessageFormat.format(HQL, propertyName, modelName);

            Query query = session.createQuery(hql);
            query.setCacheable(false); // don't interfere with cache
            query.setParameter(0, model);

            List<?> results = query.list();

            @SuppressWarnings("unchecked")
            P tmp = (P) results;
            value = tmp;
        }

        depth++;

        return value;
    }

    private boolean isLegalDepth() {
        return maxMemoryDepth == null || depth <= maxMemoryDepth;
    }
}
