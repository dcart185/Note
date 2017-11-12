package modules

import com.google.inject.AbstractModule
import repository.notes.{NotesRepository, NotesRepositoryDatabase}
import repository.person.{PersonRepository, PersonRepositoryDatabase}

class RepositoryModule extends AbstractModule {
  override def configure() = {
    bind(classOf[PersonRepository]).to(classOf[PersonRepositoryDatabase])
    bind(classOf[NotesRepository]).to(classOf[NotesRepositoryDatabase])
  }
}
