package modules

import com.google.inject.AbstractModule
import repository.person.{PersonRepository, PersonRepositoryDatabase}

class RepositoryModule extends AbstractModule {
  override def configure() = {
    bind(classOf[PersonRepository]).to(classOf[PersonRepositoryDatabase])
  }
}
