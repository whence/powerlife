package Dravadle;

public class ModelToDto {
  public Dto map(Model model) {
    Dto dto = new Dto();
    dto.setDtoName("DtoName:" + model.getModelName());
    return dto;
  }
}
