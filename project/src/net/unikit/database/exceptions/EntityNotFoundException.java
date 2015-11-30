package net.unikit.database.exceptions;

import net.unikit.database.interfaces.entities.AbstractModel;

/**
 * Created by Andreas on 29.11.2015.
 */
public class EntityNotFoundException extends Exception {
    private AbstractModel entity;

    public EntityNotFoundException(AbstractModel entity) {
        this.entity = entity;
    }

    public AbstractModel getEntity() {
        return entity;
    }
}
