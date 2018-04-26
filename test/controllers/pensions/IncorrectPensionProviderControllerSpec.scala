/*
 * Copyright 2018 HM Revenue & Customs
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

package controllers.pensions

import builders.{AuthBuilder, RequestBuilder}
import controllers.FakeTaiPlayApplication
import mocks.MockTemplateRenderer
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.PlaySpec
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.test.Helpers._
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.frontend.auth.connectors.domain.Authority
import uk.gov.hmrc.play.frontend.auth.connectors.{AuthConnector, DelegationConnector}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer
import uk.gov.hmrc.tai.config.ApplicationConfig
import uk.gov.hmrc.tai.connectors.responses.{TaiSuccessResponseWithPayload, TaiTaxAccountFailureResponse}
import uk.gov.hmrc.tai.model.TaiRoot
import uk.gov.hmrc.tai.model.domain.{EmploymentIncome, PensionIncome}
import uk.gov.hmrc.tai.model.domain.income.{Live, TaxCodeIncome, Week1Month1BasisOperation}
import uk.gov.hmrc.tai.service.{JourneyCacheService, PersonService, TaxAccountService}
import uk.gov.hmrc.tai.util.{FormValuesConstants, IncorrectPensionDecisionConstants, JourneyCacheConstants}

import scala.concurrent.Future

class IncorrectPensionProviderControllerSpec extends PlaySpec with FakeTaiPlayApplication
  with MockitoSugar
  with I18nSupport
  with JourneyCacheConstants
  with FormValuesConstants
  with IncorrectPensionDecisionConstants {

  implicit val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]


  "decision" must {
    "show the decision view" when {
      "a valid pension id has been passed" in {
        val sut = createSUT
        val pensionTaxCodeIncome = TaxCodeIncome(PensionIncome, Some(1), 100, "", "",
          "TEST", Week1Month1BasisOperation, Live)
        val empTaxCodeIncome = TaxCodeIncome(EmploymentIncome, Some(2), 100, "", "",
          "", Week1Month1BasisOperation, Live)
        when(sut.taxAccountService.taxCodeIncomes(any(), any())(any())).
          thenReturn(Future.successful(TaiSuccessResponseWithPayload(Seq(pensionTaxCodeIncome, empTaxCodeIncome))))
        when(sut.journeyCacheService.cache(any())(any())).thenReturn(Future.successful(Map("" -> "")))

        val result = sut.decision(1)(RequestBuilder.buildFakeRequestWithAuth("GET"))

        status(result) mustBe OK
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() must include(Messages("tai.incorrectPension.decision.title"))
      }
    }


    "return Internal Server error" when {
      "tax code income sources are not available" in {
        val sut = createSUT
        when(sut.taxAccountService.taxCodeIncomes(any(), any())(any())).
          thenReturn(Future.successful(TaiTaxAccountFailureResponse("Failed")))

        val result = sut.decision(1)(RequestBuilder.buildFakeRequestWithAuth("GET"))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "an invalid id has been passed" in {
        val sut = createSUT
        val pensionTaxCodeIncome = TaxCodeIncome(PensionIncome, Some(1), 100, "", "",
          "", Week1Month1BasisOperation, Live)
        val empTaxCodeIncome = TaxCodeIncome(EmploymentIncome, Some(2), 100, "", "",
          "", Week1Month1BasisOperation, Live)
        when(sut.taxAccountService.taxCodeIncomes(any(), any())(any())).
          thenReturn(Future.successful(TaiSuccessResponseWithPayload(Seq(pensionTaxCodeIncome, empTaxCodeIncome))))

        val result = sut.decision(4)(RequestBuilder.buildFakeRequestWithAuth("GET"))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }


      "an invalid pension id has been passed" in {
        val sut = createSUT
        val pensionTaxCodeIncome = TaxCodeIncome(PensionIncome, Some(1), 100, "", "",
          "", Week1Month1BasisOperation, Live)
        val empTaxCodeIncome = TaxCodeIncome(EmploymentIncome, Some(2), 100, "", "",
          "", Week1Month1BasisOperation, Live)
        when(sut.taxAccountService.taxCodeIncomes(any(), any())(any())).
          thenReturn(Future.successful(TaiSuccessResponseWithPayload(Seq(pensionTaxCodeIncome, empTaxCodeIncome))))

        val result = sut.decision(2)(RequestBuilder.buildFakeRequestWithAuth("GET"))

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }

  "handle decision" must {
    "return bad request" when {
      "no options are selected" in {
        val sut = createSUT
        when(sut.journeyCacheService.mandatoryValue(Matchers.eq(IncorrectPensionProvider_NameKey))(any())).
          thenReturn(Future.successful("TEST"))

        val result = sut.handleDecision()(RequestBuilder.buildFakeRequestWithAuth("POST").
          withFormUrlEncodedBody(IncorrectPensionDecision -> ""))

        status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to tes-1 iform" when {
      "option NO is selected" in {
        val sut = createSUT
        when(sut.journeyCacheService.mandatoryValue(Matchers.eq(IncorrectPensionProvider_NameKey))(any())).
          thenReturn(Future.successful("TEST"))

        val result = sut.handleDecision()(RequestBuilder.buildFakeRequestWithAuth("POST").
          withFormUrlEncodedBody(IncorrectPensionDecision -> NoValue))

        status(result) mustBe SEE_OTHER
        redirectLocation(result).get mustBe ApplicationConfig.incomeFromEmploymentPensionLinkUrl
      }
    }

    "redirect to whatDoYouWantToTellUs" when {
      "option YES is selected" in {
        val sut = createSUT
        when(sut.journeyCacheService.mandatoryValue(Matchers.eq(IncorrectPensionProvider_NameKey))(any())).
          thenReturn(Future.successful("TEST"))

        val result = sut.handleDecision()(RequestBuilder.buildFakeRequestWithAuth("POST").
          withFormUrlEncodedBody(IncorrectPensionDecision -> YesValue))

        status(result) mustBe SEE_OTHER
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() must include(Messages("tai.updatePension.whatDoYouWantToTellUs.title"))
      }
    }

  }

  "whatDoYouWantToTellUs" must {
    "show the whatDoYouWantToTellUs page" when {
      "an authorised user calls the page" in {
        val sut = createSUT
        when(sut.journeyCacheService.mandatoryValue(Matchers.eq(IncorrectPensionProvider_NameKey))(any())).
          thenReturn(Future.successful("TEST"))

        val result = sut.whatDoYouWantToTellUs()(RequestBuilder.buildFakeRequestWithAuth("GET"))

        status(result) mustBe OK
        val doc = Jsoup.parse(contentAsString(result))
        doc.title() must include(Messages("tai.updatePension.whatDoYouWantToTellUs.title"))
      }
    }
  }

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def createSUT = new SUT

  val generateNino: Nino = new Generator().nextNino

  class SUT extends IncorrectPensionProviderController {
    override val personService: PersonService = mock[PersonService]
    override val journeyCacheService: JourneyCacheService = mock[JourneyCacheService]
    override implicit val templateRenderer: TemplateRenderer = MockTemplateRenderer
    override implicit val partialRetriever: FormPartialRetriever = mock[FormPartialRetriever]
    override protected val delegationConnector: DelegationConnector = mock[DelegationConnector]
    override protected val authConnector: AuthConnector = mock[AuthConnector]
    override val taxAccountService: TaxAccountService = mock[TaxAccountService]
    val ad: Future[Some[Authority]] = Future.successful(Some(AuthBuilder.createFakeAuthority(generateNino.nino)))
    when(authConnector.currentAuthority(any(), any())).thenReturn(ad)
    when(personService.personDetails(any())(any())).thenReturn(Future.successful(TaiRoot("", 1, "", "", None, "", "", false, None)))
  }
}
