package utility

import java.security.{MessageDigest, SecureRandom}
import java.util.Random

import org.junit.Assert
import org.scalatest.BeforeAndAfter
import org.scalatestplus.play.PlaySpec
import utility.CryptoUtil.DecryptFailure

class CryptoUtilSpec extends PlaySpec with BeforeAndAfter{

  "Key generator" must {
    "be be able to generate a key from a password" in {
      val random : Random = new SecureRandom()
      val iv : Array[Byte] = Array.fill[Byte](32)(0)
      random.nextBytes(iv)

      val password : String = "SomeRandomPasswordIWontUse"

      val generatedKey1 : Array[Byte] = CryptoUtil.generateKeyFromDerivedByteArray(password.getBytes,iv)
      val generatedKey2 : Array[Byte] = CryptoUtil.generateKeyFromDerivedByteArray(password.getBytes,iv)
      generatedKey1 mustEqual generatedKey2
    }
  }

  "Encryption" must {
    "be able to encrypt and then decrypt the data" in {

      val toBeEncrypted: String = "Here is some random text that needs to be encrypted.  Unit test only!"
      val toBeEncryptedBytes: Array[Byte] = toBeEncrypted.getBytes()

      val random: Random = new SecureRandom()
      val iv: Array[Byte] = Array.fill[Byte](32)(0)
      val key: Array[Byte] = Array.fill[Byte](32)(0)
      random.nextBytes(iv)
      random.nextBytes(key)

      val encryptedData = CryptoUtil.encryptData(toBeEncryptedBytes, iv, key)

      val decryptedData = CryptoUtil.decryptData(encryptedData, iv, key)
      val decryptedText: String = new String(decryptedData)

      toBeEncrypted mustEqual decryptedText

    }

    "be able to mac" in {
      val toBeEncrypted: String = "Here is some random text that needs to be encrypted and mac"
      val random: Random = new SecureRandom()
      val iv: Array[Byte] = Array.fill[Byte](32)(0)
      val key: Array[Byte] = Array.fill[Byte](32)(0)
      val macKey: Array[Byte] = Array.fill[Byte](32)(0)

      val encryptedData = CryptoUtil.encryptData(toBeEncrypted.getBytes(), iv, key)

      val tag : Array[Byte] = CryptoUtil.hashMacEncryptedData(encryptedData,macKey)
      val tag2 : Array[Byte] = CryptoUtil.hashMacEncryptedData(encryptedData,macKey)

      val isEqual = MessageDigest.isEqual(tag,tag2)
      isEqual mustBe true
    }

    "be able to encrypt then mac" in {

      val toBeEncrypted: String = "testing encrypt then mac and mac then decrypt"
      val random: Random = new SecureRandom()
      val aesKey: Array[Byte] = Array.fill[Byte](32)(0)
      val macKey: Array[Byte] = Array.fill[Byte](32)(0)

      val encryptedData : Array[Byte] = CryptoUtil.encryptThenMac(toBeEncrypted.getBytes(),aesKey,macKey)
      val decryptedDataEither : Either[DecryptFailure,Array[Byte]] = CryptoUtil.macThenDecrypt(encryptedData,aesKey,macKey)

      decryptedDataEither match {
        case Left(decryptFailure) => {
          Assert.fail(decryptFailure.reason)
        }
        case Right(decryptedData) => {
          val decryptedText : String = new String(decryptedData)
          toBeEncrypted mustEqual decryptedText
        }
      }
    }
  }

}
