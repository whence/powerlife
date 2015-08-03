package Dravadle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class DravadleApp {
  public static void main(String[] args) throws JsonProcessingException {
    outLazy();
    //outStrict();
  }

  private static void outLazy() throws JsonProcessingException {
    Controller controller = new Controller(new ModelToDto(), new EntityToModel());
    List<Dto> dtos = controller.getDtosLazy();
    ObjectMapper mapper = new ObjectMapper();
    String response = mapper.writeValueAsString(dtos);
    System.out.println(response);
  }

  private static void outStrict() throws JsonProcessingException {
    Controller controller = new Controller(new ModelToDto(), new EntityToModel());
    List<Dto> dtos = controller.getDtosStrict();
    ObjectMapper mapper = new ObjectMapper();
    String response = mapper.writeValueAsString(dtos);
    System.out.println(response);
  }
}