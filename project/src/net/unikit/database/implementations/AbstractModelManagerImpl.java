package net.unikit.database.implementations;

import com.google.common.collect.ImmutableList;
import net.unikit.database.exceptions.ConstraintViolationExceptionCommon;
import net.unikit.database.exceptions.ModelNotFoundExceptionCommon;
import net.unikit.database.interfaces.entities.AbstractModel;
import net.unikit.database.interfaces.managers.AbstractModelManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractModelManagerImpl<EntityType extends AbstractModel, IdType,
        BaseEntityType extends AbstractModel, BaseIdType extends IdType> implements AbstractModelManager<EntityType, IdType> {
    private SessionFactory sessionFactory;

    protected AbstractModelManagerImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    interface TransactionAction<ResultType> {
        ResultType run(Session session) throws ModelNotFoundExceptionCommon, ConstraintViolationExceptionCommon;
    }

    private <ResultType> ResultType doTransaction(TransactionAction<ResultType> transactionAction) throws ModelNotFoundExceptionCommon, ConstraintViolationExceptionCommon {
        Session session = sessionFactory.openSession();
        Transaction transaction = null;
        ResultType result = null;

        try {
            transaction = session.beginTransaction();
            result = transactionAction.run(session);
            transaction.commit();
        } catch (HibernateException e) {
            if (transaction != null)
                transaction.rollback();

            try {
                throw e;
            } catch (org.hibernate.exception.ConstraintViolationException e1) {
                throw new ConstraintViolationExceptionCommon(e1.getCause(), null);
            }
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
        } catch (ModelNotFoundExceptionCommon | ConstraintViolationExceptionCommon e) {
            // NOTE: Those exceptions will never been thrown!
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public EntityType getEntity(IdType id) throws ModelNotFoundExceptionCommon {
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
        } catch (ConstraintViolationExceptionCommon e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updateEntity(EntityType entity) throws ModelNotFoundExceptionCommon, ConstraintViolationExceptionCommon {
        doTransaction(new TransactionAction<Void>() {
            @Override
            public Void run(Session session) throws ModelNotFoundExceptionCommon, ConstraintViolationExceptionCommon {
                BaseIdType baseId = (BaseIdType) entity.getId();
                BaseEntityType entityOld = (BaseEntityType) session.get(getAnnotatedClass(), (Serializable) baseId);
                if (entityOld == null)
                    throw new ModelNotFoundExceptionCommon(entityOld);
                updateDatabaseFields(entityOld, (BaseEntityType) entity);
                try {
                    session.update(entityOld);
                } catch (org.hibernate.exception.ConstraintViolationException e) {
                    throw new ConstraintViolationExceptionCommon(e.getCause(), entity);
                }
                return null;
            }
        });
    }

    @Override
    public void deleteEntity(EntityType entity) throws ModelNotFoundExceptionCommon {
        try {
            doTransaction(new TransactionAction<Void>() {
                @Override
                public Void run(Session session) throws ModelNotFoundExceptionCommon {
                    BaseIdType baseId = (BaseIdType) entity.getId();
                    EntityType entityOld = (EntityType) session.get(getAnnotatedClass(), (Serializable) baseId);
                    if (entityOld == null)
                        throw new ModelNotFoundExceptionCommon(entityOld);
                    session.delete(entityOld);
                    return null;
                }
            });
        } catch (ConstraintViolationExceptionCommon e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
        }
    }

    @Override
    public IdType addEntity(EntityType entity) throws ConstraintViolationExceptionCommon {
        try {
            return doTransaction(new TransactionAction<IdType>() {
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
        } catch (ModelNotFoundExceptionCommon e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
            return null;
        }
    }

    public abstract EntityType createEntity();
    protected abstract Class getAnnotatedClass();
    protected abstract void updateDatabaseFields(BaseEntityType entityOld, BaseEntityType entityNew);
}
