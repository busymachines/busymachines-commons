/**
  * Copyright (c) 2017-2018 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package busymachines.effects.sync.validation_test

import busymachines.core._
import busymachines.effects.sync.validated._
import busymachines.effects.sync._
import org.scalatest._

/**
  *
  * @author Lorand Szakacs, lsz@lorandszakacs.com, lorand.szakacs@busymachines.com
  * @since 28 Jan 2018
  *
  */
final class ValidatedEffectsSyntaxTest extends FunSpec {
  //prevents atrocious English
  private def test: ItWord = it

  private implicit class TestSyntax[T](value: Validated[T]) {
    //short for "run"
    def r: T = value.unsafeGet()
    def r(ctor: (Anomaly, List[Anomaly]) => Anomalies): T = value.unsafeGet(ctor)
  }

  //--------------------------------------------------------------------------
  private val thr:   RuntimeException          = new RuntimeException("runtime_exception")
  private val ano:   Anomaly                   = InvalidInputFailure("invalid_input_failure")
  private val anoG:  GenericValidationFailures = GenericValidationFailures(ano)
  private val anoT:  TVFs                      = TVFs(ano)
  private val anoT2: TVFs                      = TVFs(ano, List(ano))

  private val none: Option[Int] = Option.empty
  private val some: Option[Int] = Option.pure(42)

  private val success:    Try[Int] = Try(42)
  private val failureThr: Try[Int] = Try.failThr(thr)
  private val failure:    Try[Int] = Try.fail(ano)
  private val failureG:   Try[Int] = Try.fail(anoG)
  private val failureT:   Try[Int] = Try.fail(anoT)
  private val failureT2:  Try[Int] = Try.fail(anoT2)

  private val correct:     Result[Int] = Result(42)
  private val incorrect:   Result[Int] = Result.fail(ano)
  private val incorrectG:  Result[Int] = Result.fail(anoG)
  private val incorrectT:  Result[Int] = Result.fail(anoT)
  private val incorrectT2: Result[Int] = Result.fail(anoT2)

  private val thr2ano: Throwable => Anomaly = thr => ForbiddenFailure
  private val ano2ano: Anomaly   => Anomaly = thr => ForbiddenFailure
  private val ano2str: Anomaly   => String  = thr => thr.message

  private val failV: Validated[Int] = Validated.fail(ano)
  private val pureV: Validated[Int] = Validated.pure(42)

  //---------------------------------------------------------------------------

  describe("Validated — companion object syntax") {

    describe("constructors") {
      test("pure") {
        assert(Validated.pure(42).unsafeGet() == 42)
      }

      test("fail") {
        assertThrows[GenericValidationFailures](Validated.fail(ano).r)
      }

      test("fail — nel") {
        val t = intercept[GenericValidationFailures](Validated.fail(cats.data.NonEmptyList.of(ano, ano)).r)
        assert(t.messages.length == 2)
      }

      test("fail — ano") {
        assertThrows[GenericValidationFailures](Validated.fail(ano).r)
      }

      test("unit") {
        assert(Validated.unit == Validated.unit)
      }

      describe("fromOptionAno") {
        test("none") {
          val value: Validated[Int] = Validated.fromOptionAno(none, ano)
          assertThrows[GenericValidationFailures](value.r)
        }

        test("some") {
          val value: Validated[Int] = Validated.fromOptionAno(some, ano)
          assert(value.r == 42)
        }
      }

      describe("fromTryAno") {
        test("failure — thr") {
          val value: Validated[Int] = Validated.fromTryAno(failureThr)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.bad == CatastrophicError(thr))
        }

        test("failure — anomaly") {
          val value: Validated[Int] = Validated.fromTryAno(failure)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.bad == ano)
        }

        test("failure — anomalies") {
          val value: Validated[Int] = Validated.fromTryAno(failureT2)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.messages.length == 2)
        }

        test("success") {
          val value: Validated[Int] = Validated.fromTryAno(success)
          assert(value.r == 42)
        }
      }

      describe("fromResult") {
        test("incorrect — anomaly") {
          val value: Validated[Int] = Validated.fromResult(incorrect)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.bad == ano)
        }

        test("incorrect — anomalies") {
          val value: Validated[Int] = Validated.fromResult(incorrectT2)
          val t = intercept[GenericValidationFailures](value.r)
          assert(t.messages.length == 2)
        }

        test("success") {
          val value: Validated[Int] = Validated.fromResult(correct)
          assert(value.r == 42)
        }
      }

    } //end constructors

    describe("boolean") {

      describe("condAno") {
        test("false") {
          val value: Validated[Int] = Validated.condAno(
            false,
            42,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value: Validated[Int] = Validated.condAno(
            true,
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = Validated.condWith(
            false,
            pureV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — pure") {
          val value = Validated.condWith(
            true,
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = Validated.condWith(
            false,
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — fail") {
          val value = Validated.condWith(
            true,
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnTrue") {
        test("false") {
          val value = Validated.invalidOnTrue(
            false,
            ano
          )
          value.r
        }

        test("true") {
          val value = Validated.invalidOnTrue(
            true,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnFalse") {
        test("false") {
          val value = Validated.invalidOnFalse(
            false,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value = Validated.invalidOnFalse(
            true,
            ano
          )
          value.r
        }
      }

    } //end boolean

    describe("as{Effect}") {

      describe("asOptionUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            Validated.asOptionUnsafe(failV)
          )
        }

        test("fail — ano") {
          assertThrows[TVFs](
            Validated.asOptionUnsafe(failV, TVFs)
          )
        }

        test("pure") {
          assert(Validated.asOptionUnsafe(pureV) == some)
        }

        test("pure — ano") {
          assert(Validated.asOptionUnsafe(pureV, TVFs) == some)
        }

      }

      describe("asListUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            Validated.asListUnsafe(failV)
          )
        }

        test("fail — ano") {
          assertThrows[TVFs](
            Validated.asListUnsafe(failV, TVFs)
          )
        }

        test("pure") {
          assert(Validated.asListUnsafe(pureV) == List(42))
        }

        test("pure — ano") {
          assert(Validated.asListUnsafe(pureV, TVFs) == List(42))
        }

      }

      describe("asTry") {

        test("fail") {
          assert(Validated.asTry(failV) == failureG)
        }

        test("fail — ano") {
          assert(Validated.asTry(failV, TVFs) == failureT)
        }

        test("pure") {
          assert(Validated.asTry(pureV) == success)
        }

        test("pure — ano") {
          assert(Validated.asTry(pureV, TVFs) == success)
        }
      }

      describe("asResult") {

        test("fail") {
          assert(Validated.asResult(failV) == incorrectG)
        }

        test("fail — ano") {
          assert(Validated.asResult(failV, TVFs) == incorrectT)
        }

        test("pure") {
          assert(Validated.asResult(pureV) == correct)
        }

        test("pure — ano") {
          assert(Validated.asResult(pureV, TVFs) == correct)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          val t = intercept[GenericValidationFailures](Validated.unsafeGet(failV))
          assert(t == anoG)
          assert(t.id.name == GenericValidationFailuresID.name) //hack for coverage
        }

        test("fail — ano") {
          val t = intercept[TVFs](Validated.unsafeGet(failV, TVFs))
          assert(t == anoT)
          assert(t.id.name == TVFsID.name) //hack for coverage
        }

        test("pure") {
          assert(Validated.unsafeGet(pureV) == 42)
        }

        test("pure — ano") {
          assert(Validated.unsafeGet(pureV, TVFs) == 42)
        }

      }

    } //end as{Effect}

  } //end companion object syntax tests

  //===========================================================================
  //===========================================================================
  //===========================================================================

  describe("Validated — reference syntax") {

    describe("as{Effect}") {

      describe("asOptionUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            failV.asOptionUnsafe()
          )
        }

        test("fail — ano") {
          assertThrows[TVFs](
            failV.asOptionUnsafe(TVFs)
          )
        }

        test("pure") {
          assert(pureV.asOptionUnsafe() == some)
        }

        test("pure — ano") {
          assert(pureV.asOptionUnsafe(TVFs) == some)
        }

      }

      describe("asListUnsafe") {

        test("fail") {
          assertThrows[GenericValidationFailures](
            failV.asListUnsafe()
          )
        }

        test("fail — ano") {
          assertThrows[TVFs](
            failV.asListUnsafe(TVFs)
          )
        }

        test("pure") {
          assert(pureV.asListUnsafe() == List(42))
        }

        test("pure — ano") {
          assert(pureV.asListUnsafe(TVFs) == List(42))
        }
      }

      describe("asTry") {

        test("fail") {
          assert(failV.asTry == failureG)
        }

        test("fail — ano") {
          assert(failV.asTry(TVFs) == failureT)
        }

        test("pure") {
          assert(pureV.asTry == success)
        }

        test("pure — ano") {
          assert(pureV.asTry(TVFs) == success)
        }

      }

      describe("asResult") {

        test("fail") {
          assert(failV.asResult == incorrectG)
        }

        test("fail — ano") {
          assert(failV.asResult(TVFs) == incorrectT)
        }

        test("pure") {
          assert(pureV.asResult == correct)
        }

        test("pure — ano") {
          assert(pureV.asResult(TVFs) == correct)
        }

      }

      describe("unsafeGet") {

        test("fail") {
          assertThrows[GenericValidationFailures](failV.unsafeGet())
        }

        test("fail — ano") {
          assertThrows[TVFs](failV.unsafeGet(TVFs))
        }

        test("pure") {
          assert(pureV.unsafeGet() == 42)
        }

        test("pure — ano") {
          assert(pureV.unsafeGet(TVFs) == 42)
        }

      }

    } //end as{Effect}

    describe("boolean") {

      describe("cond") {
        test("false") {
          val value: Validated[Int] = false.cond(
            42,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value = true.cond(
            42,
            ano
          )
          assert(value.r == 42)
        }
      }

      describe("condWith") {
        test("false — pure") {
          val value = false.condWith(
            pureV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — pure") {
          val value = true.condWith(
            pureV,
            ano
          )
          assert(value.r == 42)
        }

        test("false — fail") {
          val value = false.condWith(
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true — fail") {
          val value = true.condWith(
            failV,
            ano
          )
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnTrue") {
        test("false") {
          val value = false.invalidOnTrue(ano)
          value.r
        }

        test("true") {
          val value = true.invalidOnTrue(ano)
          assertThrows[GenericValidationFailures](value.r)
        }
      }

      describe("invalidOnFalse") {
        test("false") {
          val value = false.invalidOnFalse(ano)
          assertThrows[GenericValidationFailures](value.r)
        }

        test("true") {
          val value = true.invalidOnFalse(ano)
          value.r
        }
      }

    } //end boolean
  } //end reference syntax tests

} //end test
