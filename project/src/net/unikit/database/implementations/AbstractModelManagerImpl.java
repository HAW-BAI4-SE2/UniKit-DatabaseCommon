package net.unikit.database.implementations;

import com.google.common.collect.ImmutableList;
import net.unikit.database.exceptions.ModelNotFoundException;
import net.unikit.database.interfaces.entities.AbstractModel;
import net.unikit.database.interfaces.managers.AbstractModelManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractModelManagerImpl<EntityType extends AbstractModel, IdType,
        BaseEntityType extends AbstractModel, BaseIdType extends Serializable> implements AbstractModelManager<EntityType, IdType> {
    private SessionFactory sessionFactory;

    protected AbstractModelManagerImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    interface TransactionAction<ResultType> {
        ResultType run(Session session) throws ModelNotFoundException;
    }

    private <ResultType> ResultType doTransaction(TransactionAction<ResultType> transactionAction) throws ModelNotFoundException {
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
            throw e;
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
        } catch (ModelNotFoundException e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public EntityType getEntity(IdType id) throws ModelNotFoundException {
        return doTransaction(new TransactionAction<EntityType>() {
            @Override
            public EntityType run(Session session) throws ModelNotFoundException {
                BaseIdType baseId = createBaseIdFromIdType(id);
                EntityType entity = (EntityType) session.get(getAnnotatedClass(), baseId);
                if (entity == null)
                    throw new ModelNotFoundException(entity);
                return entity;
            }
        });
    }

    @Override
    public void updateEntity(EntityType entity) throws ModelNotFoundException {
        doTransaction(new TransactionAction<Void>() {
            @Override
            public Void run(Session session) throws ModelNotFoundException {
                BaseIdType baseId = createBaseIdFromIdType((IdType) entity.getId());
                BaseEntityType entityOld = (BaseEntityType) session.get(getAnnotatedClass(), baseId);
                if (entityOld == null)
                    throw new ModelNotFoundException(entityOld);
                updateDatabaseFields(entityOld, (BaseEntityType) entity);
                session.update(entityOld);
                return null;
            }
        });
    }

    @Override
    public void deleteEntity(EntityType entity) throws ModelNotFoundException {
        doTransaction(new TransactionAction<Void>() {
            @Override
            public Void run(Session session) throws ModelNotFoundException {
                BaseIdType baseId = createBaseIdFromIdType((IdType) entity.getId());
                EntityType entityOld = (EntityType) session.get(getAnnotatedClass(), baseId);
                if (entityOld == null)
                    throw new ModelNotFoundException(entityOld);
                session.delete(entityOld);
                return null;
            }
        });
    }

    @Override
    public IdType addEntity(EntityType entity) {
        try {
            return doTransaction(new TransactionAction<IdType>() {
                @Override
                public IdType run(Session session) {
                    BaseIdType id = (BaseIdType) session.save(entity);
                    return createIdFromBaseIdType(id);
                }
            });
        } catch (ModelNotFoundException e) {
            // NOTE: This exception will never been thrown!
            e.printStackTrace();
            return null;
        }
    }

    public abstract EntityType createEntity();
    protected abstract IdType createIdFromBaseIdType(BaseIdType id);
    protected abstract BaseIdType createBaseIdFromIdType(IdType id);
    protected abstract Class getAnnotatedClass();
    protected abstract void updateDatabaseFields(BaseEntityType entityOld, BaseEntityType entityNew);
}
