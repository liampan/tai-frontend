/*
 * Copyright 2019 HM Revenue & Customs
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

package controllers

import builders.RequestBuilder
import controllers.actions.FakeValidatePerson
import mocks.MockTemplateRenderer
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.tai.service.journeyCache.JourneyCacheService
import play.api.test.Helpers.{contentAsString, _}
import uk.gov.hmrc.tai.util.constants.FormValuesConstants

import scala.concurrent.Future

class DuplicateSubmissionWarningControllerSpec extends PlaySpec with FakeTaiPlayApplication with MockitoSugar
 with FormValuesConstants {
  implicit val messages: MessagesApi = app.injector.instanceOf[MessagesApi]

  val journeyCacheService = mock[JourneyCacheService]
  val successfulJourneyCacheService = mock[JourneyCacheService]
  val name = "Income Name"
  val id = "123"
  private def fakePostRequest = RequestBuilder.buildFakeRequestWithAuth("POST")

  class DuplicateSubmissionWarningControllerTest extends DuplicateSubmissionWarningController(
    journeyCacheService,
    successfulJourneyCacheService,
    FakeAuthAction,
    FakeValidatePerson,
    mock[FormPartialRetriever],
    MockTemplateRenderer
  ){
    when(journeyCacheService.mandatoryValues(Matchers.anyVararg[String])(any()))
      .thenReturn(Future.successful(Seq(name, id)))
  }

  "duplicateSubmissionWarning" must {
    "show duplicateSubmissionWarning view" in {

      val controller = new DuplicateSubmissionWarningControllerTest
      val result = controller.duplicateSubmissionWarning(fakeRequest)
      val doc = Jsoup.parse(contentAsString(result))

      status(result) mustBe OK
      doc.title() must include(messages("tai.pension.warning.customGaTitle"))
    }
  }

  "submitDuplicateSubmissionWarning" must {

    "redirect to the update remove employment decision page" when {
      "I want to update my employment is selected" in {

        val controller = new DuplicateSubmissionWarningControllerTest
        val result = controller.submitDuplicateSubmissionWarning(fakePostRequest.withFormUrlEncodedBody(YesNoChoice -> YesValue))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe controllers.pensions.routes.UpdatePensionProviderController.doYouGetThisPension().url
      }
    }

    "redirect to the income source summary page" when {
      "I want to return to my employment details is selected" in {

        val controller = new DuplicateSubmissionWarningControllerTest
        val result = controller.submitDuplicateSubmissionWarning(fakePostRequest.withFormUrlEncodedBody(YesNoChoice -> NoValue))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe controllers.routes.IncomeSourceSummaryController.onPageLoad(id.toInt).url
      }
    }

    "return BadRequest" when {
      "there is a form validation error (standard form validation)" in {

        val controller = new DuplicateSubmissionWarningControllerTest
        val result = controller.submitDuplicateSubmissionWarning(fakePostRequest.withFormUrlEncodedBody(YesNoChoice -> ""))

        status(result) mustBe BAD_REQUEST
      }
    }
  }

  "submitEmploymentsDuplicateSubmissionWarning" must {
    "redirect to the update remove employment decision page" when {
      "I want to update my employment is selected" in {
        val controller = new DuplicateSubmissionWarningControllerTest

        val result = controller.submitEmploymentsDuplicateSubmissionWarning(fakePostRequest
          .withFormUrlEncodedBody(YesNoChoice -> YesValue))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe controllers.employments.routes.EndEmploymentController.employmentUpdateRemoveDecision.url
      }
    }

    "redirect to the income source summary page" when {
      "I want to return to my employment details is selected" in {
        val controller = new DuplicateSubmissionWarningControllerTest

        val result = controller.submitEmploymentsDuplicateSubmissionWarning(fakePostRequest
          .withFormUrlEncodedBody(YesNoChoice -> NoValue))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe controllers.routes.IncomeSourceSummaryController.onPageLoad(id.toInt).url
      }
    }

    "return BadRequest" when {
      "there is a form validation error (standard form validation)" in {
        val controller = new DuplicateSubmissionWarningControllerTest

        val result = controller.submitEmploymentsDuplicateSubmissionWarning(fakePostRequest
          .withFormUrlEncodedBody(YesNoChoice -> ""))

        status(result) mustBe BAD_REQUEST
      }
    }
  }

}
