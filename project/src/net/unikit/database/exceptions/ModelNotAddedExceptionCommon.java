package net.unikit.database.exceptions;

import net.unikit.database.interfaces.entities.AbstractModel;

/**
 * Created by Andreas on 12.12.2015.
 */
public class ModelNotAddedExceptionCommon extends Exception {
    private AbstractModel entity;

    public ModelNotAddedExceptionCommon(AbstractModel entity) {
        this.entity = entity;
    }

    public AbstractModel getEntity() {
        return entity;
    }
}
