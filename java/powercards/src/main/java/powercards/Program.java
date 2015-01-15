package powercards;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Configuration
@ComponentScan
public class Program {

  @Bean
  Dialog getDialog() {
    return new Dialog();
  }

  public static void main(String[] args){
    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8);
    List<Integer> twoEvenSquares =
        numbers.stream()
            .filter(n -> {
              System.out.println("filtering " + n);
              return n % 2 == 0;
            })
            .map(n -> {
              System.out.println("mapping " + n);
              return n * n;
            })
            .limit(2)
            .collect(toList());
    System.out.println(twoEvenSquares);

    ApplicationContext context =
        new AnnotationConfigApplicationContext(Program.class);
    Dialog dialog = context.getBean(Dialog.class);
    System.out.println(dialog.test());

  }
}
