package globals

import models.Person

trait PersonHelper {
  val person1 : Person = Person(None,"John","Doe","jdoe@random.com",Some("randomtext"),Some("masterkey"),None,
    Some("randomIv"))
}
