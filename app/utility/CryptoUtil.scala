package utility
import java.io._
import java.security.{MessageDigest, SecureRandom}
import java.util.{Base64, Random}

import org.bouncycastle.crypto.CipherParameters
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.engines.AESEngine
import org.bouncycastle.crypto.generators.KDF1BytesGenerator
import org.bouncycastle.crypto.macs.HMac
import org.bouncycastle.crypto.modes.CFBBlockCipher
import org.bouncycastle.crypto.params.{KDFParameters, KeyParameter, ParametersWithIV}


object CryptoUtil {
  private val defaultChunkSize = 64
  private val bitBlockSize = 8
  private val ivSize = 16
  private val macDigest = new SHA256Digest

  val keySize = 16

  case class DecryptFailure(reason:String)

  def createRandomKey():Array[Byte]={
    val random : Random = new SecureRandom()
    val key : Array[Byte] = Array.fill[Byte](keySize)(0)
    random.nextBytes(key)
    key
  }

  def generateKeyFromDerivedString(text:String):Array[Byte]={
    val random : Random = new SecureRandom()
    val iv : Array[Byte] = Array.fill[Byte](ivSize)(0)
    random.nextBytes(iv)

    generateKeyFromDerivedByteArray(text.getBytes(),iv)
  }

  /**
    *
    * @param shared this is a password, as an array of bytes, that will be used to generate the new key
    * @param iv
    * @return the key as an array of bytes
    */
  def generateKeyFromDerivedByteArray(shared:Array[Byte], iv:Array[Byte]):Array[Byte] ={
    val length : Int = 16
    val data = new Array[Byte](length)
    val digest = new SHA256Digest
    val kDF1BytesGenerator = new KDF1BytesGenerator(digest)
    val kDFParameters = new KDFParameters(shared,iv)
    kDF1BytesGenerator.init(kDFParameters)
    kDF1BytesGenerator.generateBytes(data,0,length)
    data
  }

  /**
    *
    * @param data data to be encrypted
    * @param aesKey key for symetric encryption
    * @param macKey key for the mac
    */
  def encryptThenMac(data:Array[Byte],aesKey:Array[Byte],macKey:Array[Byte]):Array[Byte]={
    //first create a random iv
    val random : Random = new SecureRandom()
    val iv : Array[Byte] = Array.fill[Byte](ivSize)(0)
    random.nextBytes(iv)

    //encrypt the data
    val encryptedData : Array[Byte] = encryptData(data,iv,aesKey)

    //mac the data
    val tag : Array[Byte] = hashMacEncryptedData(encryptedData,macKey)

    //return
    val newByteArrayLength : Int = ivSize + tag.length + encryptedData.length
    val newByteArray : Array[Byte] = iv ++ encryptedData ++ tag
    newByteArray
  }

  /**
    *
    * @param data the data to be decrypted
    * @param aesKey the key used to decrypt the data
    * @param macKey the key used to mac the encrypted data
    * @return either a failure result or the actual decrypted data
    */
  def macThenDecrypt(data:Array[Byte],aesKey:Array[Byte],macKey:Array[Byte]):Either[DecryptFailure,Array[Byte]]={
    val encryptedDataSize = data.length - (ivSize + macDigest.getDigestSize)

    val iv : Array[Byte] = data.take(ivSize)
    val encryptedData : Array[Byte] = data.slice(ivSize,ivSize+encryptedDataSize)
    val macTag : Array[Byte] = data.takeRight(macDigest.getDigestSize)

    val computedTag : Array[Byte] = hashMacEncryptedData(encryptedData,macKey)

    if(MessageDigest.isEqual(computedTag,macTag)){
      val decryptedData : Array[Byte] = decryptData(encryptedData,iv,aesKey)
      Right(decryptedData)
    }
    else{
      Left(DecryptFailure("Mac tags are not the same"))
    }
  }

  /**
    *
    * This is a simple application.  This will not work for very large packages (exceeding more than 2 gigs)
    *
    * @param data
    * @param iv a random iv should be use
    * @param key the aes key
    * @return encrypted data
    */
  def encryptData(data:Array[Byte], iv:Array[Byte], key:Array[Byte]) : Array[Byte]={
    val cfb : CFBBlockCipher = new CFBBlockCipher(new AESEngine,bitBlockSize)
    val keyParam : KeyParameter = new KeyParameter(key)
    cfb.reset()
    cfb.init(true,new ParametersWithIV(keyParam,iv))

    val chunkSize : Int= if (data.length < defaultChunkSize) data.length else defaultChunkSize
    var inBuff : Array[Byte] = Array.fill[Byte](chunkSize)(0)
    var outBuff : Array[Byte] = Array.fill[Byte](chunkSize)(0)

    val inputStream : ByteArrayInputStream = new ByteArrayInputStream(data)
    val outputStream : ByteArrayOutputStream = new ByteArrayOutputStream(data.length)

    var totalBytesRead : Long = 0
    var numberOfBytesRead : Long = 0
    while(numberOfBytesRead != -1 && totalBytesRead < data.length){
      numberOfBytesRead = inputStream.read(inBuff)

      if(numberOfBytesRead != -1){
        totalBytesRead = totalBytesRead + numberOfBytesRead
        cfb.processBytes(inBuff,0,inBuff.length,outBuff,0)
        outputStream.write(outBuff)


        if(data.length < totalBytesRead + inBuff.length){
          val newSize : Int = data.length - totalBytesRead.toInt
          inBuff = Array.fill[Byte](newSize)(0)
          outBuff = Array.fill[Byte](newSize)(0)
        }
      }
    }
    outputStream.flush()
    outputStream.close()
    outputStream.toByteArray
  }


  /**
    *
    * @param data the encrypted data you wish to mac
    * @param key the key for the mac
    * @return
    */
  def hashMacEncryptedData(data:Array[Byte],key:Array[Byte]):Array[Byte] ={
    val hmac : HMac = new HMac(macDigest)
    val parameters : CipherParameters = new KeyParameter(key)
    hmac.init(parameters)

    val tag = Array.fill[Byte](macDigest.getDigestSize)(0)

    var count = 0

    val chunkSize : Int= if (data.length < defaultChunkSize) data.length else defaultChunkSize
    var inBuff = Array.fill[Byte](chunkSize)(0)

    val inputStream : ByteArrayInputStream = new ByteArrayInputStream(data)

    var numberOfBytesRead = inputStream.read(inBuff)
    var totalBytesRead = 0
    while(numberOfBytesRead != -1){
      hmac.update(inBuff,0,numberOfBytesRead)
      totalBytesRead = totalBytesRead + numberOfBytesRead

      if(data.length < totalBytesRead + chunkSize){
        val newChunkSize = data.length-totalBytesRead
        inBuff = Array.fill[Byte](newChunkSize)(0)
      }

      numberOfBytesRead = inputStream.read(inBuff)
    }

    val sizeAsString : String = totalBytesRead.toString
    val sizeInBytes : Array[Byte] = sizeAsString.getBytes
    hmac.update(sizeInBytes,0,sizeInBytes.length)
    hmac.doFinal(tag,0)
    tag
  }


  /**
    *
    * @param data encrypted data
    * @param iv
    * @param key the aes key
    * @return decrypted data
    */
  def decryptData(data:Array[Byte], iv:Array[Byte], key:Array[Byte]) : Array[Byte]= {
    val cfb : CFBBlockCipher = new CFBBlockCipher(new AESEngine,bitBlockSize)
    val keyParam : KeyParameter = new KeyParameter(key)
    cfb.reset()
    cfb.init(false,new ParametersWithIV(keyParam,iv))

    val chunkSize : Int= if (data.length < defaultChunkSize) data.length else defaultChunkSize
    var inBuff : Array[Byte] = Array.fill[Byte](chunkSize)(0)
    var outBuff : Array[Byte] = Array.fill[Byte](chunkSize)(0)

    val inputStream : ByteArrayInputStream = new ByteArrayInputStream(data)
    val outputStream : ByteArrayOutputStream = new ByteArrayOutputStream(data.length)

    var totalBytesRead : Long = 0
    var numberOfBytesRead : Long = 0
    while(numberOfBytesRead != -1 && totalBytesRead < data.length){
      numberOfBytesRead = inputStream.read(inBuff)

      if(numberOfBytesRead != -1){
        totalBytesRead = totalBytesRead + numberOfBytesRead
        cfb.processBytes(inBuff,0,inBuff.length,outBuff,0)
        outputStream.write(outBuff)


        if(data.length < totalBytesRead + inBuff.length){
          val newSize : Int = data.length - totalBytesRead.toInt
          inBuff = Array.fill[Byte](newSize)(0)
          outBuff = Array.fill[Byte](newSize)(0)
        }
      }
    }
    outputStream.flush()
    outputStream.close()
    outputStream.toByteArray
  }
}
