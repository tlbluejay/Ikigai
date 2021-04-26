package lewis.trenton.ikigai.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import lewis.trenton.ikigai.models.Task;

public interface TaskJpaRepository extends JpaRepository<Task, Long> {

}
