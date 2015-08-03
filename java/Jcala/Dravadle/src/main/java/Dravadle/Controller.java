package Dravadle;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {
  private ModelToDto modelToDto;
  private EntityToModel entityToModel;

  public Controller(ModelToDto modelToDto, EntityToModel entityToModel) {
    this.modelToDto = modelToDto;
    this.entityToModel = entityToModel;
  }

  private final Function<Model, Dto> modelToDtoFunction =
      new Function<Model, Dto>() {
        @Override
        public Dto apply(Model model) {
          return modelToDto.map(model);
        }
    };

  public List<Dto> getDtosLazy() {
    List<Model> models = getModels();

    return Lists.transform(models, modelToDtoFunction);
  }

  public List<Dto> getDtosStrict() {
    List<Model> models = getModels();

    return models.stream().map(modelToDto::map).collect(Collectors.toList());
  }

  private List<Model> getModels() {
    List<Entity> entities = getEntities();

    return Lists.transform(entities, new Function<Entity, Model>() {
      @Override
      public Model apply(Entity entity) {
        return entityToModel.map(entity);
      }
    });
  }

  private List<Entity> getEntities() {
    Entity entity1 = new Entity();
    entity1.setEntityName("EntityName1");

    Entity entity2 = new Entity();
    entity2.setEntityName("EntityName2");

    Entity entity3 = new Entity();
    entity3.setEntityName("EntityName3");

    return Arrays.asList(entity1, entity2, entity3);
  }
}
