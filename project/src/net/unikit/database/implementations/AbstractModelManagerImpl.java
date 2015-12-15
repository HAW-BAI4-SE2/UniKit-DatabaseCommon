package net.unikit.database.implementations;

import com.google.common.collect.ImmutableList;
import net.unikit.database.exceptions.ConstraintViolationExceptionCommon;
import net.unikit.database.exceptions.MissingPropertyExceptionCommon;
import net.unikit.database.exceptions.ModelNotAddedExceptionCommon;
import net.unikit.database.exceptions.ModelNotFoundExceptionCommon;
import net.unikit.database.interfaces.entities.AbstractModel;
import net.unikit.database.interfaces.managers.AbstractModelManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AbstractModelManagerImpl<EntityType extends AbstractModel, IdType,
        BaseEntityType extends AbstractModel, BaseIdType extends IdType> implements AbstractModelManager<EntityType, IdType> {
    private SessionFactory sessionFactory;

    protected AbstractModelManagerImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    interface TransactionAction<ResultType> {
        ResultType run(Session session) throws ModelNotFoundExceptionCommon, ConstraintViolationExceptionCommon, ModelNotAddedExceptionCommon;
    }

    private <ResultType> ResultType doTransaction(TransactionAction<ResultType> transactionAction) throws ConstraintViolationExceptionCommon, ModelNotFoundExceptionCommon, ModelNotAddedExceptionCommon, MissingPropertyExceptionCommon {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        ResultType result = null;

        try {
            transaction = session.beginTransaction();
            result = transactionAction.run(session);
            transaction.commit();
        } catch (org.hibernate.PropertyValueException e) {
            if (transaction != null)
                transaction.rollback();

            // TODO: Refactor! Will only be thrown by addEntity and updateEntity right now!!!
            throw new MissingPropertyExceptionCommon(e.getCause(), null);
        } catch (org.hibernate.exception.ConstraintViolationException e) {
            if (transaction != null)
                transaction.rollback();

            // TODO: Refactor! Will only be thrown by addEntity right now!!!
            throw new ConstraintViolationExceptionCommon(e.getCause(), null);
        } catch (HibernateException hibernateException) {
            if (transaction != null)
                transaction.rollback();

            throw hibernateException;
        } catch (ConstraintViolationExceptionCommon constraintViolationExceptionCommon) {
            if (transaction != null)
                transaction.rollback();

            throw constraintViolationExceptionCommon;
        } catch (ModelNotFoundExceptionCommon modelNotFoundExceptionCommon) {
            if (transaction != null)
                transaction.rollback();

            throw modelNotFoundExceptionCommon;
        } catch (ModelNotAddedExceptionCommon modelNotAddedExceptionCommon) {
            if (transaction != null)
                transaction.rollback();

            throw modelNotAddedExceptionCommon;
        } finally {
            if (session != null)
                session.close();
        }

        return result;
    }

    @Override
    public List<EntityType> getAllEntities() {
        try {
            return doTransaction(new TransactionAction<List<EntityType>>() {
                @Override
                public List<EntityType> run(Session session) {
                    List<EntityType> entities = session.createQuery("FROM " + getAnnotatedClass().getSimpleName()).list();
                    return ImmutableList.copyOf(entities);
                }
            });
        } catch (ModelNotFoundExceptionCommon | ConstraintViolationExceptionCommon | ModelNotAddedExceptionCommon | MissingPropertyExceptionCommon e) {
            // NOTE: Those exceptions will never been thrown!
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public EntityType getEntity(IdType id) throws ModelNotFoundExceptionCommon {
        checkNotNull(id);

        try {
            return doTransaction(new TransactionAction<EntityType>() {
                @Override
                public EntityType run(Session session) throws ModelNotFoundExceptionCommon {
                    BaseIdType baseId = (BaseIdType) id;
                    EntityType entity = (EntityType) session.get(getAnnotatedClass(), (Serializable) baseId);
                    if (entity == null)
                        throw new ModelNotFoundExceptionCommon(entity);
                    return entity;
                }
            });
        } catch (ConstraintViolationExceptionCommon | ModelNotAddedExceptionCommon | MissingPropertyExceptionCommon e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateEntity(EntityType entity) throws ModelNotFoundExceptionCommon, ConstraintViolationExceptionCommon, ModelNotAddedExceptionCommon, MissingPropertyExceptionCommon {
        checkNotNull(entity);

        doTransaction(new TransactionAction<Void>() {
            @Override
            public Void run(Session session) throws ModelNotFoundExceptionCommon, ConstraintViolationExceptionCommon, ModelNotAddedExceptionCommon {
                BaseIdType baseId = (BaseIdType) entity.getId();
                BaseEntityType entityOld = null;
                try {
                    entityOld = (BaseEntityType) session.get(getAnnotatedClass(), (Serializable) baseId);
                } catch (IllegalArgumentException e) {
                    throw new ModelNotAddedExceptionCommon(entity);
                }
                if (entityOld == null)
                    throw new ModelNotFoundExceptionCommon(entityOld);

                updateDatabaseFields(entityOld, (BaseEntityType) entity);
                try {
                    session.update(entityOld);
                } catch (org.hibernate.exception.ConstraintViolationException e) {
                    throw new ConstraintViolationExceptionCommon(e.getCause(), entity);
                }

                // Update auto generated fields
                updateAutogeneratedFields((BaseEntityType) entity, entityOld);

                return null;
            }
        });
    }

    @Override
    public void deleteEntity(EntityType entity) throws ModelNotFoundExceptionCommon, ModelNotAddedExceptionCommon {
        checkNotNull(entity);

        try {
            doTransaction(new TransactionAction<Void>() {
                @Override
                public Void run(Session session) throws ModelNotFoundExceptionCommon, ModelNotAddedExceptionCommon {
                    BaseIdType baseId = (BaseIdType) entity.getId();
                    EntityType entityOld = null;
                    try {
                        entityOld = (EntityType) session.get(getAnnotatedClass(), (Serializable) baseId);
                    } catch (IllegalArgumentException e) {
                        throw new ModelNotAddedExceptionCommon(entity);
                    }
                    if (entityOld == null)
                        throw new ModelNotFoundExceptionCommon(entityOld);

                    session.delete(entityOld);
                    return null;
                }
            });
        } catch (ConstraintViolationExceptionCommon | MissingPropertyExceptionCommon e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
        }
    }

    @Override
    public IdType addEntity(EntityType entity) throws ConstraintViolationExceptionCommon, MissingPropertyExceptionCommon {
        checkNotNull(entity);

        IdType result = null;

        // Add entity to database
        try {
            result = doTransaction(new TransactionAction<IdType>() {
                @Override
                public IdType run(Session session) throws ConstraintViolationExceptionCommon {
                    BaseIdType id = null;
                    try {
                        id = (BaseIdType) session.save(entity);
                    } catch (org.hibernate.exception.ConstraintViolationException e) {
                        throw new ConstraintViolationExceptionCommon(e.getCause(), entity);
                    }

                    return id;
                }
            });
        } catch (ModelNotFoundExceptionCommon | ModelNotAddedExceptionCommon e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
            return null;
        }

        // Update auto generated fields
        try {
            final IdType finalResult = result;
            doTransaction(new TransactionAction<Void>() {
                @Override
                public Void run(Session session) throws ModelNotFoundExceptionCommon {
                    BaseIdType baseId = (BaseIdType) finalResult;
                    EntityType entityNew = (EntityType) session.get(getAnnotatedClass(), (Serializable) baseId);
                    updateAutogeneratedFields((BaseEntityType) entity, (BaseEntityType) entityNew);

                    return null;
                }
            });
        } catch (ModelNotFoundExceptionCommon | ConstraintViolationExceptionCommon | ModelNotAddedExceptionCommon | MissingPropertyExceptionCommon e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
            return null;
        }

        return result;
    }

    public abstract EntityType createEntity();
    protected abstract Class getAnnotatedClass();
    protected abstract void updateDatabaseFields(BaseEntityType entityOld, BaseEntityType entityNew);
    protected abstract void updateAutogeneratedFields(BaseEntityType entityOld, BaseEntityType entityNew);
}
