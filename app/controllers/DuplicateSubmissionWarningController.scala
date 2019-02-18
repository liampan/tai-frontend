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

import com.google.inject.Inject
import com.google.inject.name.Named
import controllers.actions.ValidatePerson
import controllers.auth.AuthAction
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.play.partials.FormPartialRetriever
import uk.gov.hmrc.renderer.TemplateRenderer
import uk.gov.hmrc.tai.forms.pensions.DuplicateSubmissionWarningForm
import uk.gov.hmrc.tai.service.journeyCache.JourneyCacheService
import uk.gov.hmrc.tai.util.constants.JourneyCacheConstants
import play.api.i18n.Messages.Implicits._
import uk.gov.hmrc.tai.util.constants.FormValuesConstants
import play.api.Play.current

import scala.concurrent.Future

class DuplicateSubmissionWarningController @Inject()(@Named("Update Pension Provider") journeyCacheService: JourneyCacheService,
                                                     @Named("Track Successful Journey") successfulJourneyCacheService: JourneyCacheService,
                                                     authenticate: AuthAction,
                                                     validatePerson: ValidatePerson,
                                                     override implicit val partialRetriever: FormPartialRetriever,
                                                     override implicit val templateRenderer: TemplateRenderer) extends TaiBaseController
  with JourneyCacheConstants
  with FormValuesConstants {

  def duplicateSubmissionWarning: Action[AnyContent] = (authenticate andThen validatePerson).async {
    implicit request =>
      implicit val user = request.taiUser
      journeyCacheService.mandatoryValues(UpdatePensionProvider_NameKey, UpdatePensionProvider_IdKey) map { mandatoryValues =>
        Ok(views.html.pensions.duplicateSubmissionWarning(DuplicateSubmissionWarningForm.createForm, mandatoryValues(0), mandatoryValues(1).toInt))
      }
  }

  def submitDuplicateSubmissionWarning: Action[AnyContent] = (authenticate andThen validatePerson).async {
    implicit request =>
      implicit val user = request.taiUser
      journeyCacheService.mandatoryValues(UpdatePensionProvider_NameKey, UpdatePensionProvider_IdKey) flatMap { mandatoryValues =>
        DuplicateSubmissionWarningForm.createForm.bindFromRequest.fold(
          formWithErrors => {
            Future.successful(BadRequest(views.html.pensions.
              duplicateSubmissionWarning(formWithErrors, mandatoryValues(0), mandatoryValues(1).toInt)))
          },
          success => {
            success.yesNoChoice match {
              case Some(YesValue) => Future.successful(Redirect(controllers.pensions.routes.UpdatePensionProviderController.
                doYouGetThisPension()))
              case Some(NoValue) =>
                Future.successful(Redirect(controllers.routes.IncomeSourceSummaryController.
                  onPageLoad(mandatoryValues(1).toInt)))
            }
          }
        )
      }
  }

    def submitEmploymentsDuplicateSubmissionWarning: Action[AnyContent] = (authenticate andThen validatePerson).async {
      implicit request =>
        implicit val user = request.taiUser
        journeyCacheService.mandatoryValues(EndEmployment_NameKey, EndEmployment_EmploymentIdKey) flatMap { mandatoryValues =>
          DuplicateSubmissionWarningForm.createForm.bindFromRequest.fold(
            formWithErrors => {
              Future.successful(BadRequest(views.html.employments.
                duplicateSubmissionWarning(formWithErrors, mandatoryValues(0), mandatoryValues(1).toInt)))
            },
            success => {
              success.yesNoChoice match {
                case Some(YesValue) => Future.successful(Redirect(controllers.employments.routes.EndEmploymentController.
                  employmentUpdateRemoveDecision()))
                case Some(NoValue) => Future.successful(Redirect(controllers.routes.IncomeSourceSummaryController.
                  onPageLoad(mandatoryValues(1).toInt)))
              }
            }
          )
        }
    }
}
