package me.shadaj.scalapy.py

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

abstract class ValueAndRequestObject(val value: PyValue) {
  protected def getObject: Object

  private var objectCache: Object = null
  final def requestObject: Object = {
    if (objectCache == null) objectCache = getObject
    objectCache
  }
}

trait ObjectReader[T] {
  def read(r: ValueAndRequestObject): T
}

object ObjectReader extends ObjectTupleReaders {
  implicit val wrapperReader = new ObjectReader[Object] {
    def read(r: ValueAndRequestObject): Object = r.requestObject
  }

  implicit val wrapperDynReader = new ObjectReader[Dynamic] {
    def read(r: ValueAndRequestObject): Dynamic = r.requestObject.asDynamic
  }

  implicit def facadeReader[F <: ObjectFacade](implicit creator: FacadeCreator[F]): ObjectReader[F] = new ObjectReader[F] {
    override def read(r: ValueAndRequestObject): F = {
      val inst = creator.create
      inst.value = r.value
      inst
    }
  }

  def toByte(value: Any): Byte = {
    value match {
      case b: Byte => b
      case i: Int => if (i <= Byte.MaxValue && i >= Byte.MaxValue) i.toByte else {
        throw new IllegalArgumentException("Tried to convert a Int outside Byte range to an Byte")
      }
      case l: Long => if (l <= Byte.MaxValue && l >= Byte.MinValue) l.toByte else {
        throw new IllegalArgumentException("Tried to convert a Long outside Byte range to an Byte")
      }
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to a Byte")
      case _: Float =>
        throw new IllegalArgumentException("Cannot up-convert a Float to a Byte")
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  def toInt(value: Any): Int = {
    value match {
      case b: Byte => b
      case i: Int => i
      case l: Long => if (l <= Int.MaxValue && l >= Int.MinValue) l.toInt else {
        throw new IllegalArgumentException("Tried to convert a Long outside Int range to an Int")
      }
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to an Int")
      case _: Float =>
        throw new IllegalArgumentException("Cannot up-convert a Float to an Int")
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}: $value")
    }
  }

  def toLong(value: Any): Long = {
    value match {
      case b: Byte => b
      case i: Int => i
      case l: Long => l
      case _: Double =>
        throw new IllegalArgumentException("Cannot up-convert a Double to a Long")
      case _: Float =>
        throw new IllegalArgumentException("Cannot up-convert a Float to a Long")
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  def toDouble(value: Any): Double = {
    value match {
      case i: Int => i
      case l: Long => l
      case d: Double => d
      case f: Float => f
      case s: String => s.toDouble
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass} for value $value")
    }
  }

  def toFloat(value: Any): Float = {
    value match {
      case i: Int => i
      case l: Long => l
      case d: Double =>
        if (d.toFloat == d) d.toFloat else {
          throw new IllegalArgumentException("Cannot up-convert a Double to a Float")
        }
      case fl: Float => fl
      case _ =>
        throw new IllegalArgumentException(s"Unknown type: ${value.getClass}")
    }
  }

  implicit val unitReader = new ObjectReader[Unit] {
    def read(r: ValueAndRequestObject): Unit = ()
  }

  implicit val byteReader = new ObjectReader[Byte] {
    def read(r: ValueAndRequestObject): Byte = toByte(r.value.getLong)
  }

  implicit val intReader = new ObjectReader[Int] {
    def read(r: ValueAndRequestObject): Int = toInt(r.value.getLong)
  }

  implicit val longReader = new ObjectReader[Long] {
    def read(r: ValueAndRequestObject): Long = toLong(r.value.getLong)
  }

  implicit val doubleReader = new ObjectReader[Double] {
    def read(r: ValueAndRequestObject): Double = toDouble(r.value.getDouble)
  }

  implicit val floatReader = new ObjectReader[Float] {
    def read(r: ValueAndRequestObject): Float = toFloat(r.value.getDouble)
  }

  implicit val booleanReader = new ObjectReader[Boolean] {
    def read(r: ValueAndRequestObject): Boolean = {
      r.value.getBoolean
    }
  }

  implicit val stringReader = new ObjectReader[String] {
    def read(r: ValueAndRequestObject): String = r.value.getString
  }

  implicit def seqReader[T](implicit reader: ObjectReader[T]): ObjectReader[Seq[T]] = new ObjectReader[Seq[T]] {
    def read(r: ValueAndRequestObject) = {
      r.value.getSeq.zipWithIndex.map { case (v, i) =>
        reader.read(new ValueAndRequestObject(v) {
          def getObject = r.requestObject.asDynamic.arrayAccess(i)
        })
      }.toSeq
    }
  }

  implicit def mapReader[I, O](implicit readerI: ObjectReader[I], readerO: ObjectReader[O]): ObjectReader[Map[I, O]] = new ObjectReader[Map[I, O]] {
    override def read(r: ValueAndRequestObject): Map[I, O] = {
      r.value.getMap.map { case (k, v) =>
        readerI.read(new ValueAndRequestObject(k) {
          def getObject = throw new IllegalAccessException("Cannot read a Python object for the key of a map")
        }) -> readerO.read(new ValueAndRequestObject(v) {
          def getObject = {
            if (Platform.isNative) ??? else {
              r.requestObject.asDynamic.dictionaryAccess(
                Object.populateWith(interpreter.asInstanceOf[JepInterpreter].valueFromAny(k))
              )
            }
          }
        })
      }.toMap
    }
  }
}