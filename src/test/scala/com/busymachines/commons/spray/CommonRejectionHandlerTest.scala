package com.busymachines.commons.spray

import org.junit.runner.RunWith
import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import spray.http._
import spray.testkit.{ScalatestRouteTest, RouteTest}


/**
 * Created by lorand on 09.07.2014.
 */
@RunWith(classOf[JUnitRunner])
class CommonRejectionHandlerTest extends FlatSpec with RouteTest with ScalatestRouteTest {

  val userAuthRequestBodyJson = """
  {
    "loginName": "user1@test.com",
    "password": "test"
  }
                                """
  // TODO write test without depending on prefab

//  "CommonRejectionHandler" should "list the rejection exceptions when an internal server error happens" in {
//    var authResponse: AuthenticationResponse = null
//    var mediaId: String = null
//
//    Post("/users/authentication", HttpEntity(ContentTypes.`application/json`, userAuthRequestBodyJson.replace(",", "#"))) ~> authenticationApiV1.route ~> check {
//
//
//      intercept[Throwable] {
//        assert(s"$body".contains("MalformedRequestContentRejection"))
//        assert(status === StatusCodes.InternalServerError)
//      }
//
//
//    }


//  }
}
