package org.openapitools.mapper;

import org.openapitools.entity.LampEntity;
import org.openapitools.model.Lamp;
import org.springframework.stereotype.Component;

@Component
public class LampMapper {

  public Lamp toModel(LampEntity entity) {
    if (entity == null) {
      return null;
    }
    return new Lamp(entity.getId(), entity.getStatus());
  }

  public LampEntity toEntity(Lamp lamp) {
    if (lamp == null) {
      return null;
    }
    return new LampEntity(lamp.getId(), lamp.getStatus());
  }

  public LampEntity toEntity(Boolean status) {
    return new LampEntity(status);
  }
}
