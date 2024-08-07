package io.github.dhi13man.spring.datasource.generators;

import io.github.dhi13man.spring.datasource.annotations.TargetSecondaryDataSource;
import io.github.dhi13man.spring.datasource.generators.generated.repositories.read_replica.ReadReplicaMockConfigTestRepository;
import io.github.dhi13man.spring.datasource.generators.generated.repositories.replica_2.Replica2MockRepositoryTestRepository;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

class TargetSecondaryDataSourceGeneratorTest {

  @Test
  void generateRepositoryTypeElementWithAnnotatedMethods() {
    // Arrange
    final Class<MockRepositoryTestRepository> mockConfigTestRepositoryClass =
        MockRepositoryTestRepository.class;
    final Class<ReadReplicaMockConfigTestRepository> readReplicaGeneratedClass =
        ReadReplicaMockConfigTestRepository.class;
    final Class<Replica2MockRepositoryTestRepository> replica2GeneratedClass =
        Replica2MockRepositoryTestRepository.class;

    // Assert
    // Classes Generated
    Assertions.assertTrue(ReflectionUtils.isPublic(readReplicaGeneratedClass));
    Assertions.assertTrue(ReflectionUtils.isPublic(replica2GeneratedClass));

    // Find By Custom Object Id should be in MockConfigTestRepository and
    // Replica2MockConfigTestRepository, but not in ReadReplicaMockConfigTestRepository
    final Optional<Method> findByCustomObjectIdMockConfigTestRepository = ReflectionUtils.findMethod(
        mockConfigTestRepositoryClass,
        "findByCustomObjectId",
        long.class
    );
    final Optional<Method> findByCustomObjectIdReadReplicaMockConfigTestRepository = ReflectionUtils.findMethod(
        readReplicaGeneratedClass,
        "findByCustomObjectId",
        long.class
    );
    final Optional<Method> findByCustomObjectIdReplica2MockConfigTestRepository = ReflectionUtils.findMethod(
        replica2GeneratedClass,
        "findByCustomObjectId",
        long.class
    );
    Assertions.assertTrue(findByCustomObjectIdMockConfigTestRepository.isPresent());
    Assertions.assertFalse(findByCustomObjectIdReadReplicaMockConfigTestRepository.isPresent());
    Assertions.assertTrue(findByCustomObjectIdReplica2MockConfigTestRepository.isPresent());

    // Find All should be in MockConfigTestRepository, ReadReplicaMockConfigTestRepository and
    // Replica2MockConfigTestRepository (as it overrides the findAll method of JpaRepository).
    //
    // However, the findAll method of Replica2MockConfigTestRepository will throw an exception
    // as it is annotated only with @TargetSecondaryDataSource("read-replica"). Hence, the method
    // is disabled in the Replica2MockConfigTestRepository generated class.
    final Optional<Method> findAllMockConfigTestRepository = ReflectionUtils.findMethod(
        mockConfigTestRepositoryClass,
        "findAll"
    );
    final Optional<Method> findAllReadReplicaMockConfigTestRepository = ReflectionUtils.findMethod(
        readReplicaGeneratedClass,
        "findAll"
    );
    final Optional<Method> findAllReplica2MockConfigTestRepository = ReflectionUtils.findMethod(
        replica2GeneratedClass,
        "findAll"
    );
    Assertions.assertTrue(findAllMockConfigTestRepository.isPresent());
    Assertions.assertTrue(findAllReadReplicaMockConfigTestRepository.isPresent());
    Assertions.assertTrue(findAllReplica2MockConfigTestRepository.isPresent());
  }


  public interface MockRepositoryTestRepository extends JpaRepository<String, Long> {

    @TargetSecondaryDataSource("replica-2")
    Object findByCustomObjectId(long customObjectId);

    @Override
    @TargetSecondaryDataSource("read-replica")
    @NonNull
    List<String> findAll();
  }

}