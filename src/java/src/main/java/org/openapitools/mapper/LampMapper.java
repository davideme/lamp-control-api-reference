package org.openapitools.mapper;

import org.openapitools.entity.LampEntity;
import org.openapitools.model.Lamp;
import org.springframework.stereotype.Component;

@Component
public class LampMapper {

  public Lamp toModel(final LampEntity entity) {
    Lamp result = null;
    if (entity != null) {
      result = new Lamp();
      // Set fields using setters because generated model Lamp does not have a (UUID,
      // Boolean) constructor
      result.setId(entity.getId());
      result.setStatus(entity.getStatus());
      // createdAt/updatedAt are left null; persistence layer should populate them if
      // needed
    }
    return result;
  }

  public LampEntity toEntity(final Lamp lamp) {
    LampEntity result = null;
    if (lamp != null) {
      result = new LampEntity(lamp.getId(), lamp.getStatus());
    }
    return result;
  }

  public LampEntity toEntity(final Boolean status) {
    return new LampEntity(status);
  }
}
