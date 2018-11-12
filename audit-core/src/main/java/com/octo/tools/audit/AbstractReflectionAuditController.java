package com.octo.tools.audit;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;

import javax.persistence.Id;

import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

public abstract class AbstractReflectionAuditController<T, R> extends AbstractAuditController<T, R> {

	private Field entityIdField;
	private Method entityIdGetter;

	private Field revEntityIdField;
	private Method revEntityIdGetter;
	private Field revEntityTimestampField;
	private Method revEntityTimestampGetter;

	public AbstractReflectionAuditController(Class<T> entityClass,
			Class<? extends AbstractReflectionAuditController<T,R>> controller) {
		super(entityClass, controller);
	}

	protected Long getEntityId(T entity) {
		if (entityIdField != null)
			return getId(entity, entityIdField);
		if (entityIdGetter != null)
			return getId(entity, entityIdGetter);
		Field[] fields = entityClass.getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(Id.class)) {
				entityIdField = f;
				return getId(entity, f);
			}
		}
		for (Method m : entityClass.getMethods()) {
			if (m.isAnnotationPresent(Id.class)) {
				entityIdGetter = m;
				return getId(entity, m);
			}
		}
		return null;
	}

	protected Long getRevisionEntityId(R revEntity) {
		if (revEntityIdField != null)
			return getId(revEntity, revEntityIdField);
		if (revEntityIdGetter != null)
			return getId(revEntity, revEntityIdGetter);
		Field[] fields = revEntity.getClass().getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(RevisionNumber.class)) {
				revEntityIdField = f;
				return getId(revEntity, f);
			}
		}
		for (Method m : revEntity.getClass().getMethods()) {
			if (m.isAnnotationPresent(RevisionNumber.class)) {
				revEntityIdGetter = m;
				return getId(revEntity, m);
			}
		}
		return null;
	}

	private Date getTimestamp(R entity, Method m) {
		try {
			if (m.getReturnType().equals(Date.class))
				return (Date) m.invoke(entity, (Object[])null);
			else
				return new Date((Long) m.invoke(entity, (Object[])null));

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new AuditConfigurationException(e);
		}
	}

	private Date getTimestamp(R entity, Field f) {
		boolean b = false;
		try {
			if (!f.canAccess(entity)) {
				f.setAccessible(true);
				b = true;
			}
			if (f.getType().equals(Date.class))
				return (Date) f.get(entity);
			else
				return new Date((Long) f.get(entity));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new AuditConfigurationException(e);
		} finally {
			if (b)
				f.setAccessible(false);
		}
	}

	private Long getId(Object entity, Method m) {
		try {
			if(Long.class.equals(m.getReturnType()) || long.class.equals(m.getReturnType()))
				return (Long) m.invoke(entity, (Object[])null);
			else if(Integer.class.equals(m.getReturnType()) || int.class.equals(m.getReturnType()))
				return ((Integer) m.invoke(entity, (Object[])null)).longValue();
			return null;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new AuditConfigurationException(e);
		}
	}

	private Long getId(Object entity, Field f) throws AuditConfigurationException {
		boolean b = false;
		try {
			if (!f.canAccess(entity)) {
				f.setAccessible(true);
				b = true;
			}
			if(Long.class.equals(f.getType()) || long.class.equals(f.getType()))
				return (Long) f.get(entity);
			else if(Integer.class.equals(f.getType()) || int.class.equals(f.getType()))
				return ((Integer) f.get(entity)).longValue();
			return null;
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new AuditConfigurationException(e);
		} finally {
			if (b)
				f.setAccessible(false);
		}
	}

	@Override
	protected Date getRevisionEntityTimestamp(R revEntity) {
		if (revEntityTimestampField != null)
			return getTimestamp(revEntity, revEntityTimestampField);
		if (revEntityIdGetter != null)
			return getTimestamp(revEntity, revEntityTimestampGetter);
		Field[] fields = revEntity.getClass().getDeclaredFields();
		for (Field f : fields) {
			if (f.isAnnotationPresent(RevisionTimestamp.class)) {
				revEntityTimestampField = f;
				return getTimestamp(revEntity, f);
			}
		}
		for (Method m : revEntity.getClass().getMethods()) {
			if (m.isAnnotationPresent(RevisionTimestamp.class)) {
				revEntityTimestampGetter = m;
				return getTimestamp(revEntity, m);
			}
		}
		return null;
	}

}
