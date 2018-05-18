package com.octo.tools.audit;

import static org.hibernate.envers.internal.entities.mapper.relation.query.QueryConstants.MIDDLE_ENTITY_ALIAS;
import static org.hibernate.envers.internal.entities.mapper.relation.query.QueryConstants.REVISION_PARAMETER;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.configuration.internal.AuditEntitiesConfiguration;
import org.hibernate.envers.configuration.internal.GlobalConfiguration;
import org.hibernate.envers.internal.entities.mapper.PersistentCollectionChangeData;
import org.hibernate.envers.internal.entities.mapper.relation.MiddleComponentData;
import org.hibernate.envers.internal.entities.mapper.relation.MiddleIdData;
import org.hibernate.envers.internal.synchronization.SessionCacheCleaner;
import org.hibernate.envers.internal.tools.query.Parameters;
import org.hibernate.envers.internal.tools.query.QueryBuilder;
import org.hibernate.envers.strategy.ValidityAuditStrategy;
import org.hibernate.event.spi.EventSource;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.property.access.spi.Getter;
import org.hibernate.sql.Update;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoExceptionAuditStrategy extends ValidityAuditStrategy {
	
	private static final Logger logger =  LoggerFactory.getLogger(NoExceptionAuditStrategy.class);
	
	/**
	 * getter for the revision entity field annotated with @RevisionTimestamp
	 */
	private Getter revisionTimestampGetter = null;

	private final SessionCacheCleaner sessionCacheCleaner;

	public NoExceptionAuditStrategy() {
		sessionCacheCleaner = new SessionCacheCleaner();
	}

	@Override
	public void perform(
			final Session session,
			final String entityName,
			final EnversService enversService,
			final Serializable id,
			final Object data,
			final Object revision) {
		final AuditEntitiesConfiguration audEntitiesCfg = enversService.getAuditEntitiesConfiguration();
		final String auditedEntityName = audEntitiesCfg.getAuditEntityName( entityName );
		final String revisionInfoEntityName = enversService.getAuditEntitiesConfiguration().getRevisionInfoEntityName();

		// Save the audit data
		session.save( auditedEntityName, data );

		// Update the end date of the previous row.
		//
		// When application reuses identifiers of previously removed entities:
		// The UPDATE statement will no-op if an entity with a given identifier has been
		// inserted for the first time. But in case a deleted primary key value was
		// reused, this guarantees correct strategy behavior: exactly one row with
		// null end date exists for each identifier.
		final boolean reuseEntityIdentifier = enversService.getGlobalConfiguration().isAllowIdentifierReuse();
		if ( reuseEntityIdentifier || getRevisionType( enversService, data ) != RevisionType.ADD ) {
			// Register transaction completion process to guarantee execution of UPDATE statement after INSERT.
			( (EventSource) session ).getActionQueue().registerProcess( new BeforeTransactionCompletionProcess() {
				@Override
				public void doBeforeTransactionCompletion(final SessionImplementor sessionImplementor) {
					
					try {
						final Queryable productionEntityQueryable = getQueryable( entityName, sessionImplementor );
						final Queryable rootProductionEntityQueryable = getQueryable(
								productionEntityQueryable.getRootEntityName(), sessionImplementor
						);
						final Queryable auditedEntityQueryable = getQueryable( auditedEntityName, sessionImplementor );
						final Queryable rootAuditedEntityQueryable = getQueryable(
								auditedEntityQueryable.getRootEntityName(), sessionImplementor
						);

						final String updateTableName;
						if ( UnionSubclassEntityPersister.class.isInstance( rootProductionEntityQueryable ) ) {
							// this is the condition causing all the problems in terms of the generated SQL UPDATE
							// the problem being that we currently try to update the in-line view made up of the union query
							//
							// this is extremely hacky means to get the root table name for the union subclass style entities.
							// hacky because it relies on internal behavior of UnionSubclassEntityPersister
							// !!!!!! NOTICE - using subclass persister, not root !!!!!!
							updateTableName = auditedEntityQueryable.getSubclassTableName( 0 );
						}
						else {
							updateTableName = rootAuditedEntityQueryable.getTableName();
						}

						final Type revisionInfoIdType = sessionImplementor.getFactory().getMetamodel().entityPersister( revisionInfoEntityName ).getIdentifierType();
						final String revEndColumnName = rootAuditedEntityQueryable.toColumns( enversService.getAuditEntitiesConfiguration().getRevisionEndFieldName() )[0];

						final boolean isRevisionEndTimestampEnabled = enversService.getAuditEntitiesConfiguration().isRevisionEndTimestampEnabled();

						// update audit_ent set REVEND = ? [, REVEND_TSTMP = ?] where (prod_ent_id) = ? and REV <> ? and REVEND is null
						final Update update = new Update( sessionImplementor.getFactory().getServiceRegistry().getService( JdbcServices.class ).getDialect() ).setTableName( updateTableName );
						// set REVEND = ?
						update.addColumn( revEndColumnName );
						// set [, REVEND_TSTMP = ?]
						if ( isRevisionEndTimestampEnabled ) {
							update.addColumn(
									rootAuditedEntityQueryable.toColumns( enversService.getAuditEntitiesConfiguration().getRevisionEndTimestampFieldName() )[0]
							);
						}

						// where (prod_ent_id) = ?
						update.addPrimaryKeyColumns( rootProductionEntityQueryable.getIdentifierColumnNames() );
						// where REV <> ?
						update.addWhereColumn(
								rootAuditedEntityQueryable.toColumns( enversService.getAuditEntitiesConfiguration().getRevisionNumberPath() )[0], "<> ?"
						);
						// where REVEND is null
						update.addWhereColumn( revEndColumnName, " is null" );

						// Now lets execute the sql...
						final String updateSql = update.toStatementString();

						int rowCount = ( (Session) sessionImplementor ).doReturningWork(
								new ReturningWork<Integer>() {
									@Override
									public Integer execute(Connection connection) throws SQLException {
										PreparedStatement preparedStatement = sessionImplementor
												.getJdbcCoordinator().getStatementPreparer().prepareStatement( updateSql );

										try {
											int index = 1;

											// set REVEND = ?
											final Number revisionNumber = enversService.getRevisionInfoNumberReader().getRevisionNumber(
													revision
											);
											revisionInfoIdType.nullSafeSet(
													preparedStatement, revisionNumber, index, sessionImplementor
											);
											index += revisionInfoIdType.getColumnSpan( sessionImplementor.getFactory() );

											// set [, REVEND_TSTMP = ?]
											if ( isRevisionEndTimestampEnabled ) {
												final Object revEndTimestampObj = revisionTimestampGetter.get( revision );
												final Date revisionEndTimestamp = convertRevEndTimestampToDate( revEndTimestampObj );
												final Type revEndTsType = rootAuditedEntityQueryable.getPropertyType(
														enversService.getAuditEntitiesConfiguration().getRevisionEndTimestampFieldName()
												);
												revEndTsType.nullSafeSet(
														preparedStatement, revisionEndTimestamp, index, sessionImplementor
												);
												index += revEndTsType.getColumnSpan( sessionImplementor.getFactory() );
											}

											// where (prod_ent_id) = ?
											final Type idType = rootProductionEntityQueryable.getIdentifierType();
											idType.nullSafeSet( preparedStatement, id, index, sessionImplementor );
											index += idType.getColumnSpan( sessionImplementor.getFactory() );

											// where REV <> ?
											final Type revType = rootAuditedEntityQueryable.getPropertyType(
													enversService.getAuditEntitiesConfiguration().getRevisionNumberPath()
											);
											revType.nullSafeSet( preparedStatement, revisionNumber, index, sessionImplementor );

											// where REVEND is null
											// 		nothing to bind....

											return sessionImplementor
													.getJdbcCoordinator().getResultSetReturn().executeUpdate( preparedStatement );
										} catch(Exception e) {
											logger.error("Exception in Audit...", e);
											return -1;
										}
										finally {
											sessionImplementor.getJdbcCoordinator().getLogicalConnection().getResourceRegistry().release(
													preparedStatement
											);
											sessionImplementor.getJdbcCoordinator().afterStatementExecution();
										}
									}
								}
						);

						if ( rowCount != 1 && ( !reuseEntityIdentifier || ( getRevisionType( enversService, data ) != RevisionType.ADD ) ) ) {
							logger.error(
									"Cannot update previous revision for entity " + auditedEntityName + " and id " + id
							);
						}
					} catch (Exception e) {
						logger.error("Exception in Audit...", e);
					}
				}
			});
		}
		sessionCacheCleaner.scheduleAuditDataRemoval( session, data );
	}

	private Queryable getQueryable(String entityName, SessionImplementor sessionImplementor) {
		return (Queryable) sessionImplementor.getFactory().getMetamodel().entityPersister( entityName );
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public void performCollectionChange(
			Session session,
			String entityName,
			String propertyName,
			EnversService enversService,
			PersistentCollectionChangeData persistentCollectionChangeData, Object revision) {
		final QueryBuilder qb = new QueryBuilder( persistentCollectionChangeData.getEntityName(), MIDDLE_ENTITY_ALIAS );

		final String originalIdPropName = enversService.getAuditEntitiesConfiguration().getOriginalIdPropName();
		final Map<String, Object> originalId = (Map<String, Object>) persistentCollectionChangeData.getData().get(
				originalIdPropName
		);
		final String revisionFieldName = enversService.getAuditEntitiesConfiguration().getRevisionFieldName();
		final String revisionTypePropName = enversService.getAuditEntitiesConfiguration().getRevisionTypePropName();

		// Adding a parameter for each id component, except the rev number and type.
		for ( Map.Entry<String, Object> originalIdEntry : originalId.entrySet() ) {
			if ( !revisionFieldName.equals( originalIdEntry.getKey() ) && !revisionTypePropName.equals( originalIdEntry.getKey() ) ) {
				qb.getRootParameters().addWhereWithParam(
						originalIdPropName + "." + originalIdEntry.getKey(),
						true, "=", originalIdEntry.getValue()
				);
			}
		}

		final SessionFactoryImplementor sessionFactory = ((SessionImplementor) session).getFactory();
		final Type propertyType = sessionFactory.getMetamodel().entityPersister( entityName ).getPropertyType( propertyName );
		if ( propertyType.isCollectionType() ) {
			CollectionType collectionPropertyType = (CollectionType) propertyType;
			// Handling collection of components.
			if ( collectionPropertyType.getElementType( sessionFactory ) instanceof ComponentType ) {
				// Adding restrictions to compare data outside of primary key.
				for ( Map.Entry<String, Object> dataEntry : persistentCollectionChangeData.getData().entrySet() ) {
					if ( !originalIdPropName.equals( dataEntry.getKey() ) ) {
						qb.getRootParameters().addWhereWithParam( dataEntry.getKey(), true, "=", dataEntry.getValue() );
					}
				}
			}
		}

		addEndRevisionNullRestriction( enversService, qb.getRootParameters() );

		final List<Object> l = qb.toQuery( session ).setLockOptions( LockOptions.UPGRADE ).list();

		// Update the last revision if one exists.
		// HHH-5967: with collections, the same element can be added and removed multiple times. So even if it's an
		// ADD, we may need to update the last revision.
		if ( l.size() > 0 ) {
			updateLastRevision(
					session, enversService, l, originalId, persistentCollectionChangeData.getEntityName(), revision
			);
		}

		// Save the audit data
		session.save( persistentCollectionChangeData.getEntityName(), persistentCollectionChangeData.getData() );
		sessionCacheCleaner.scheduleAuditDataRemoval( session, persistentCollectionChangeData.getData() );
	}

	private void addEndRevisionNullRestriction(EnversService enversService, Parameters rootParameters) {
		rootParameters.addWhere( enversService.getAuditEntitiesConfiguration().getRevisionEndFieldName(), true, "is", "null", false );
	}

	public void addEntityAtRevisionRestriction(
			GlobalConfiguration globalCfg, QueryBuilder rootQueryBuilder,
			Parameters parameters, String revisionProperty, String revisionEndProperty, boolean addAlias,
			MiddleIdData idData, String revisionPropertyPath, String originalIdPropertyName,
			String alias1, String alias2, boolean inclusive) {
		addRevisionRestriction( parameters, revisionProperty, revisionEndProperty, addAlias, inclusive );
	}

	public void addAssociationAtRevisionRestriction(
			QueryBuilder rootQueryBuilder, Parameters parameters, String revisionProperty,
			String revisionEndProperty, boolean addAlias, MiddleIdData referencingIdData,
			String versionsMiddleEntityName, String eeOriginalIdPropertyPath, String revisionPropertyPath,
			String originalIdPropertyName, String alias1, boolean inclusive, MiddleComponentData... componentDatas) {
		addRevisionRestriction( parameters, revisionProperty, revisionEndProperty, addAlias, inclusive );
	}

	public void setRevisionTimestampGetter(Getter revisionTimestampGetter) {
		this.revisionTimestampGetter = revisionTimestampGetter;
	}

	private void addRevisionRestriction(
			Parameters rootParameters, String revisionProperty, String revisionEndProperty,
			boolean addAlias, boolean inclusive) {
		// e.revision <= _revision and (e.endRevision > _revision or e.endRevision is null)
		Parameters subParm = rootParameters.addSubParameters( "or" );
		rootParameters.addWhereWithNamedParam( revisionProperty, addAlias, inclusive ? "<=" : "<", REVISION_PARAMETER );
		subParm.addWhereWithNamedParam(
				revisionEndProperty + ".id", addAlias, inclusive ? ">" : ">=", REVISION_PARAMETER
		);
		subParm.addWhere( revisionEndProperty, addAlias, "is", "null", false );
	}

	@SuppressWarnings({"unchecked"})
	private RevisionType getRevisionType(EnversService enversService, Object data) {
		return (RevisionType) ((Map<String, Object>) data).get( enversService.getAuditEntitiesConfiguration().getRevisionTypePropName() );
	}

	@SuppressWarnings({"unchecked"})
	private void updateLastRevision(
			Session session,
			EnversService enversService,
			List<Object> l,
			Object id,
			String auditedEntityName,
			Object revision) {
		// There should be one entry
		if ( l.size() == 1 ) {
			// Setting the end revision to be the current rev
			Object previousData = l.get( 0 );
			String revisionEndFieldName = enversService.getAuditEntitiesConfiguration().getRevisionEndFieldName();
			((Map<String, Object>) previousData).put( revisionEndFieldName, revision );

			if ( enversService.getAuditEntitiesConfiguration().isRevisionEndTimestampEnabled() ) {
				// Determine the value of the revision property annotated with @RevisionTimestamp
				String revEndTimestampFieldName = enversService.getAuditEntitiesConfiguration().getRevisionEndTimestampFieldName();
				Object revEndTimestampObj = this.revisionTimestampGetter.get( revision );
				Date revisionEndTimestamp = convertRevEndTimestampToDate( revEndTimestampObj );

				// Setting the end revision timestamp
				((Map<String, Object>) previousData).put( revEndTimestampFieldName, revisionEndTimestamp );
			}

			// Saving the previous version
			session.save( auditedEntityName, previousData );
			sessionCacheCleaner.scheduleAuditDataRemoval( session, previousData );
		}
		else {
			logger.error( "Cannot find previous revision for entity " + auditedEntityName + " and id " + id );
		}
	}

	private Date convertRevEndTimestampToDate(Object revEndTimestampObj) {
		// convert to a java.util.Date
		if ( revEndTimestampObj instanceof Date ) {
			return (Date) revEndTimestampObj;
		}
		return new Date( (Long) revEndTimestampObj );
	}
	
}
