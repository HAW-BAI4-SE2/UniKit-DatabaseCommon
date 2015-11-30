package net.unikit.database.exceptions;

import net.unikit.database.interfaces.entities.AbstractModel;

/**
 * Created by Andreas on 30.11.2015.
 */
public final class ConstraintViolationExceptionCommon extends Exception {
    private AbstractModel entity;

    public ConstraintViolationExceptionCommon(Throwable cause, AbstractModel entity) {
        super(cause);
        this.entity = entity;
    }

    public AbstractModel getEntity() {
        return entity;
    }
}
