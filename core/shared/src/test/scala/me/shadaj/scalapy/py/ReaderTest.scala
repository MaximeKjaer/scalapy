package me.shadaj.scalapy.py

import org.scalatest.funsuite.AnyFunSuite

class ReaderTest extends AnyFunSuite {
  test("Reading a boolean") {
    local {
      assert(!py"False".as[Boolean])
      assert(py"True".as[Boolean])
    }
  }

  test("Reading a byte") {
    local {
      assert(py"5".as[Byte] == 5.toByte)
    }
  }

  test("Reading an integer") {
    local {
      assert(py"123".as[Int] == 123)
    }
  }

  test("Reading a long") {
    local {
      assert(eval(Long.MaxValue.toString).as[Long] == Long.MaxValue)
    }
  }

  test("Reading a float") {
    local {
      assert(py"123.123".as[Float] == 123.123f)
    }
  }

  test("Reading a double") {
    local {
      assert(py"123.123".as[Double] == 123.123d)
    }
  }

  test("Reading a string") {
    local {
      assert(py"'hello world!'".as[String] == "hello world!")
    }
  }

  test("Reading an empty sequence") {
    local {
      assert(py"[]".as[Seq[Int]].isEmpty)
    }
  }

  test("Reading a sequence of ints") {
    local {
      assert(py"[1, 2, 3]".as[Seq[Int]] == Seq(1, 2, 3))
    }
  }

  test("Reading a sequence of doubles") {
    local {
      assert(py"[1.1, 2.2, 3.3]".as[Seq[Double]] == Seq(1.1, 2.2, 3.3))
    }
  }

  test("Reading a sequence of strings") {
    local {
      assert(py"['hello', 'world']".as[Seq[String]] == Seq("hello", "world"))
    }
  }

  test("Reading a sequence of sequences") {
    local {
      assert(py"[[1], [2]]".as[Seq[Seq[Int]]] == Seq(Seq(1), Seq(2)))
    }
  }

  test("Reading a sequence of objects preserves original object") {
    local {
      val datetime = module("datetime")
      val datesSeq = py"[$datetime.date.today(), $datetime.date.today().replace(year = 1000)]".as[Seq[Object]]
      assert(datesSeq.head.as[Dynamic].year.as[Int] > 2000)
      assert(datesSeq.last.as[Dynamic].year.as[Int] == 1000)
    }
  }

  test("Reading from a sequence-like object works") {
    local {
      val arr = py"'abc'"
      assert(arr.as[Seq[Char]] == Seq('a', 'b', 'c'))
    }
  }

  test("Reading a map of int to int") {
    local {
      val read = py"{ 1: 2, 2: 3 }".as[Map[Int, Int]]
      assert(read(1) == 2)
      assert(read(2) == 3)
    }
  }

  test("Reading a tuple") {
    local {
      assert(py"(1, 2)".as[(Int, Int)] == (1, 2))
    }
  }
}
