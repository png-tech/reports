package org.echosoft.framework.reports.model.providers;

import java.lang.reflect.Method;

import org.echosoft.common.collections.issuers.ReadAheadIssuer;
import org.echosoft.common.utils.ObjectUtil;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.el.Expression;
import org.echosoft.framework.reports.processor.ReportProcessingException;

/**
 * Предназначен для динамического конструирования поставщиков данных на основе контекста выполнения отчета.
 *
 * @author Anton Sharapov
 */
public class ClassDataProviderHolder implements DataProviderHolder {

    private final String id;
    private Expression object;
    private Expression methodName;
    private Expression filter;
    private Expression filterType;

    public ClassDataProviderHolder(final String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    public Expression getObject() {
        return object;
    }
    public void setObject(final Expression object) {
        this.object = object;
    }

    public Expression getMethodName() {
        return methodName;
    }
    public void setMethodName(final Expression methodName) {
        this.methodName = methodName;
    }

    public Expression getFilter() {
        return filter;
    }
    public void setFilter(final Expression filter) {
        this.filter = filter;
    }

    public Expression getFilterType() {
        return filterType;
    }
    public void setFilterType(final Expression filterType) {
        this.filterType = filterType;
    }

    @Override
    public ReadAheadIssuer getIssuer(final ELContext ctx) throws Exception {
        final Object data = resolveServiceData(ctx);
        return Issuers.asIssuer(data);
    }

    private Object resolveServiceData(final ELContext ctx) throws Exception {
        Object service = this.object != null ? this.object.getValue(ctx) : null;
        if (service instanceof String) {
            service = ObjectUtil.makeInstance((String) service, Object.class);
        }
        if (service == null)
            throw new ReportProcessingException("Service object not specified");
        final Class<?> cls = service.getClass();

        Object tmp = this.methodName != null ? this.methodName.getValue(ctx) : null;
        if (!(tmp instanceof String))
            throw new ReportProcessingException("Service method not specified or has invalid class: " + tmp);
        final String methodName = (String)tmp;

        final Class<?> argCls;
        final Object arg = this.filter != null ? this.filter.getValue(ctx) : null;
        if (arg != null) {
            argCls = arg.getClass();
        } else {
            tmp = this.filterType != null ? this.filterType.getValue(ctx) : null;
            if (tmp instanceof Class) {
                argCls = (Class)tmp;
            } else
            if (tmp instanceof String) {
                argCls = Class.forName((String)tmp);
            } else
            if (tmp == null) {
                argCls = null;
            } else
                throw new ReportProcessingException("Invalid query type: " + tmp);
        }

        if (argCls != null) {
            final Method method = cls.getMethod(methodName, argCls);
            return method.invoke(service, arg);
        } else {
            final Method method = cls.getMethod(methodName);
            return method.invoke(service);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
