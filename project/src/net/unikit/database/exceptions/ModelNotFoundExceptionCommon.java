package net.unikit.database.exceptions;

import net.unikit.database.interfaces.entities.AbstractModel;

import java.io.Serializable;

/**
 * Created by Andreas on 29.11.2015.
 */
public class ModelNotFoundExceptionCommon extends Exception {
    private AbstractModel entity;
    private Object id;

    public ModelNotFoundExceptionCommon(AbstractModel entity) {
        this.entity = entity;
        if (entity != null)
            this.id = entity.getId();
        else
            this.id = null;
    }

    public ModelNotFoundExceptionCommon(Serializable id) {
        this.entity = null;
        this.id = id;
    }

    public AbstractModel getEntity() {
        return entity;
    }

    public Object getId() {
        return id;
    }
}
