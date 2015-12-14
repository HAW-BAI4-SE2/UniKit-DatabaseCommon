package net.unikit.database.exceptions;

import net.unikit.database.interfaces.entities.AbstractModel;

/**
 * Created by Andreas on 14.12.2015.
 */
public final class MissingPropertyExceptionCommon extends Exception {
    private AbstractModel entity;

    public MissingPropertyExceptionCommon(Throwable cause, AbstractModel entity) {
        super(cause);
        this.entity = entity;
    }

    public AbstractModel getEntity() {
        return entity;
    }
}
