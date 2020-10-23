package top.siyile.facusapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FacusApiApplication implements CommandLineRunner {
	@Autowired
	private EntryRepository repository;

	public static void main(String[] args) {
		SpringApplication.run(FacusApiApplication.class, args);
	}

	@Override
	public void run(String... args) {

		repository.deleteAll();

		repository.save(new Entry("Cat", "Meow"));
		repository.save(new Entry("Dog", "Woof"));
		repository.save(new Entry("Sora", "Meow"));

		System.out.println("Entries found with findAll()");
		for (Entry entry: repository.findAll()) {
			System.out.println(entry);
		}
		System.out.println();

		System.out.println("Entry found by name Cat");
		System.out.println("----------------------");
		System.out.println(repository.findByKey("Cat"));

		System.out.println();

		System.out.println("Entry found by last name Woof");
		System.out.println("---------------------");
		for (Entry entry : repository.findByValue("Woof")) {
			System.out.println(entry);
		}
	}
}
