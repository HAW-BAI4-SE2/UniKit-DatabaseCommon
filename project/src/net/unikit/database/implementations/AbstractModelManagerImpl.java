package net.unikit.database.implementations;

import com.google.common.collect.ImmutableList;
import net.unikit.database.interfaces.entities.AbstractModel;
import net.unikit.database.interfaces.managers.AbstractModelManager;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.util.List;

public abstract class AbstractModelManagerImpl<EntityType extends AbstractModel, IdType,
        BaseEntityType, BaseIdType extends Serializable> implements AbstractModelManager<EntityType, IdType> {
    private SessionFactory sessionFactory;

    protected AbstractModelManagerImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    interface TransactionAction<ResultType> {
        ResultType run(Session session);
    }

    private <ResultType> ResultType doTransaction(TransactionAction<ResultType> transactionAction) {
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
            e.printStackTrace();
        } finally {
            if (session != null)
                session.close();
        }

        return result;
    }

    @Override
    public List<EntityType> getAllEntities() {
        return doTransaction(new TransactionAction<List<EntityType>>() {
            @Override
            public List<EntityType> run(Session session) {
                List<EntityType> entities = session.createQuery("FROM " + getAnnotatedClass().getSimpleName()).list();
                return ImmutableList.copyOf(entities);
            }
        });
    }

    @Override
    public EntityType getEntity(IdType id) {
        return doTransaction(new TransactionAction<EntityType>() {
            @Override
            public EntityType run(Session session) {
                BaseIdType baseId = createBaseIdFromIdType(id);
                EntityType entity = (EntityType) session.get(getAnnotatedClass(), baseId);
                return entity;
            }
        });
    }

    @Override
    public void updateEntity(EntityType entity) {
        doTransaction(new TransactionAction<Void>() {
            @Override
            public Void run(Session session) {
                BaseIdType baseId = createBaseIdFromIdType((IdType) entity.getId());
                BaseEntityType entityOld = (BaseEntityType) session.get(getAnnotatedClass(), baseId);
                updateDatabaseFields(entityOld, (BaseEntityType) entity);
                session.update(entityOld);
                return null;
            }
        });
    }

    @Override
    public void deleteEntity(EntityType entity) {
        doTransaction(new TransactionAction<Void>() {
            @Override
            public Void run(Session session) {
                BaseIdType baseId = createBaseIdFromIdType((IdType) entity.getId());
                EntityType entityOld = (EntityType) session.get(getAnnotatedClass(), baseId);
                session.delete(entityOld);
                return null;
            }
        });
    }

    @Override
    public IdType addEntity(EntityType entity) {
        return doTransaction(new TransactionAction<IdType>() {
            @Override
            public IdType run(Session session) {
                BaseIdType id = (BaseIdType) session.save(entity);
                return createIdFromBaseIdType(id);
            }
        });
    }

    public abstract EntityType createEntity();
    protected abstract IdType createIdFromBaseIdType(BaseIdType id);
    protected abstract BaseIdType createBaseIdFromIdType(IdType id);
    protected abstract Class getAnnotatedClass();
    protected abstract void updateDatabaseFields(BaseEntityType entityOld, BaseEntityType entityNew);
}
