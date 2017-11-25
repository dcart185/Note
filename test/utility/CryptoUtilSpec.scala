package utility

import java.security.SecureRandom
import java.util.Random

import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec

class CryptoUtilSpec extends PlaySpec with BeforeAndAfter{

  "Key generator" must {
    "be be able to generate a key from a password" in {
      val random : Random = new SecureRandom()
      val iv : Array[Byte] = Array.fill[Byte](16)(0)
      random.nextBytes(iv)

      val password : String = "SomeRandomPasswordIWontUse"

      val generatedKey1 : Array[Byte] = CryptoUtil.generateKeyFromDevivedString(password.getBytes,iv)
      val generatedKey2 : Array[Byte] = CryptoUtil.generateKeyFromDevivedString(password.getBytes,iv)
      generatedKey1 mustEqual generatedKey2
    }
  }

  "Encryption" must {
    "be able to encrypt and then decrypt the data" in {

      val toBeEncrypted: String = "Here is some random text that needs to be encrypted.  Unit test only!"
      val toBeEncryptedBytes: Array[Byte] = toBeEncrypted.getBytes()
      println(s"text:$toBeEncrypted")
      println(s"text length:${toBeEncryptedBytes.length}")

      val random: Random = new SecureRandom()
      val iv: Array[Byte] = Array.fill[Byte](16)(0)
      val key: Array[Byte] = Array.fill[Byte](16)(0)
      random.nextBytes(iv)
      random.nextBytes(key)

      val encryptedData = CryptoUtil.encryptData(toBeEncryptedBytes, iv, key)
      println(s"Encrypted length:${encryptedData.length}")

      val decryptedData = CryptoUtil.decryptData(encryptedData, iv, key)
      println(s"The decrpyted length:${decryptedData.length}")
      val decryptedText: String = new String(decryptedData)

      println(s"The decrypted text:$decryptedText")
      toBeEncrypted mustEqual decryptedText

    }
  }

}
