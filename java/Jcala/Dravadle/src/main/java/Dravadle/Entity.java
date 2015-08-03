package Dravadle;

import java.io.IOException;
import java.io.Serializable;

public class Entity implements Serializable {
  private String entityName;

  public String getEntityName() {
    return entityName;
  }

  public void setEntityName(String entityName) {
    this.entityName = entityName;
  }

  private void writeObject(java.io.ObjectOutputStream out)
      throws IOException {

  }
}
