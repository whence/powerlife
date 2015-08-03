package Dravadle;

public class EntityToModel {
  public Model map(Entity entity) {
    Model model = new Model();
    model.setModelName("ModelName:" + entity.getEntityName());
    return model;
  }
}
