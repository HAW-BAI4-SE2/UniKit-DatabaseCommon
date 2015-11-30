package net.unikit.database.exceptions;

import net.unikit.database.interfaces.entities.AbstractModel;

import java.io.Serializable;

/**
 * Created by Andreas on 29.11.2015.
 */
public final class ModelNotFoundExceptionCommon extends Exception {
    private AbstractModel entity;
    private Serializable id;

    public ModelNotFoundExceptionCommon(AbstractModel entity) {
        this.entity = entity;
        this.id = entity.getId();
    }

    public ModelNotFoundExceptionCommon(Serializable id) {
        this.entity = null;
        this.id = id;
    }

    public AbstractModel getEntity() {
        return entity;
    }

    public Serializable getId() {
        return id;
    }
}
