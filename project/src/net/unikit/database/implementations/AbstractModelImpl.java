package net.unikit.database.implementations;

import net.unikit.database.interfaces.entities.AbstractModel;

/**
 * Created by Andreas on 01.12.2015.
 */
public abstract class AbstractModelImpl<IdType> implements AbstractModel<IdType> {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractModelImpl)) return false;

        AbstractModelImpl<?> that = (AbstractModelImpl<?>) o;

        return !(getId() != null ? !getId().equals(that.getId()) : that.getId() != null);

    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName().replace("Impl", "") + "{" +
                "id=" + getId() +
                '}';
    }
}
