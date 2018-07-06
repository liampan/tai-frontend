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

package views.html.estimatedIncomeTax

import controllers.routes
import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.play.language.LanguageUtils.Dates
import uk.gov.hmrc.tai.model.domain.TaxAccountSummary
import uk.gov.hmrc.tai.model.domain.calculation.CodingComponent
import uk.gov.hmrc.tai.model.domain.income.{NonTaxCodeIncome, OtherNonTaxCodeIncome, TaxCodeIncome}
import uk.gov.hmrc.tai.model.domain.tax.{IncomeCategory, TaxBand, TotalTax}
import uk.gov.hmrc.tai.util.BandTypesConstants
import uk.gov.hmrc.tai.util.viewHelpers.TaiViewSpec
import uk.gov.hmrc.tai.viewModels._
import uk.gov.hmrc.tai.viewModels.estimatedIncomeTax.{DetailedIncomeTaxEstimateViewModel, ZeroTaxView}
import uk.gov.hmrc.time.TaxYearResolver

class detailedIncomeTaxEstimateSpec extends TaiViewSpec with BandTypesConstants {

  "view" must {

    behave like pageWithTitle(messages("tai.estimatedIncome.detailedEstimate.title"))
    behave like pageWithHeader(messages("tai.estimatedIncome.detailedEstimate.heading"))
    behave like pageWithBackLink
  }

  "show correct header content" in {

    val expectedTaxYearString =  Messages("tai.taxYear",
      nonBreakable(Dates.formatDate(TaxYearResolver.startOfCurrentTaxYear)),
      nonBreakable(Dates.formatDate(TaxYearResolver.endOfCurrentTaxYear)) )

    val accessiblePreHeading = doc.select("""header span[class="visuallyhidden"]""")
    accessiblePreHeading.text mustBe Messages("tai.estimatedIncome.accessiblePreHeading")

    val preHeading = doc.select("header p")
    preHeading.text mustBe s"${Messages("tai.estimatedIncome.accessiblePreHeading")} $expectedTaxYearString"
  }

  "have a heading for the Total Income Tax Estimate" in {
    doc(view) must haveH2HeadingWithText(messages("tai.incomeTax.totalIncomeTaxEstimate") + " £18,573")
  }


  "display table headers" in {
    doc must haveThWithText(messages("tai.incomeTax.calculated.table.headingOne"))
    doc must haveThWithText(messages("tai.incomeTax.calculated.table.headingTwo"))
    doc must haveThWithText(messages("tai.incomeTax.calculated.table.headingThree"))
    doc must haveThWithText(messages("tai.incomeTax.calculated.table.headingFour"))
  }

  "display table body" when {
    "UK user have non-savings" in {
      val nonSavings = List(
        TaxBand("B", "", 32010, 6402, None, None, 20),
        TaxBand("D0", "", 36466, 36466, None, None, 40)
      )
      val viewWithSavings: Html = views.html.howIncomeTaxIsCalculated(TaxExplanationViewModel(nonSavings, Seq.empty[TaxBand], Seq.empty[TaxBand], UkBands))
      doc(viewWithSavings) must haveTdWithText("32,010")
      doc(viewWithSavings) must haveTdWithText(messages("uk.bandtype.B"))
      doc(viewWithSavings) must haveTdWithText("20%")
      doc(viewWithSavings) must haveTdWithText("6,402.00")
      doc(viewWithSavings) must haveTdWithText("36,466")
      doc(viewWithSavings) must haveTdWithText(messages("uk.bandtype.D0"))
      doc(viewWithSavings) must haveTdWithText("40%")
      doc(viewWithSavings) must haveTdWithText("36,466.00")
    }

    "Scottish user have non-savings" in {
      val nonSavings = List(
        TaxBand("B", "", 32010, 6402, None, None, 20),
        TaxBand("D0", "", 36466, 36466, None, None, 40)
      )
      val viewWithSavings: Html = views.html.howIncomeTaxIsCalculated(TaxExplanationViewModel(nonSavings, Seq.empty[TaxBand], Seq.empty[TaxBand], ScottishBands))
      doc(viewWithSavings) must haveTdWithText("32,010")
      doc(viewWithSavings) must haveTdWithText(messages("scottish.bandtype.B"))
      doc(viewWithSavings) must haveTdWithText("20%")
      doc(viewWithSavings) must haveTdWithText("6,402.00")
      doc(viewWithSavings) must haveTdWithText("36,466")
      doc(viewWithSavings) must haveTdWithText(messages("scottish.bandtype.D0"))
      doc(viewWithSavings) must haveTdWithText("40%")
      doc(viewWithSavings) must haveTdWithText("36,466.00")
    }

    "UK user have savings" in {
      val savings = List(
        TaxBand("LSR", "", 32010, 6402, None, None, 20),
        TaxBand("HSR1", "", 36466, 36466, None, None, 40)
      )
      val viewWithSavings: Html = views.html.howIncomeTaxIsCalculated(TaxExplanationViewModel(Seq.empty[TaxBand], savings, Seq.empty[TaxBand], UkBands))
      doc(viewWithSavings) must haveTdWithText("32,010")
      doc(viewWithSavings) must haveTdWithText(messages("uk.bandtype.LSR"))
      doc(viewWithSavings) must haveTdWithText("20%")
      doc(viewWithSavings) must haveTdWithText("6,402.00")
      doc(viewWithSavings) must haveTdWithText("36,466")
      doc(viewWithSavings) must haveTdWithText(messages("uk.bandtype.HSR1"))
      doc(viewWithSavings) must haveTdWithText("40%")
      doc(viewWithSavings) must haveTdWithText("36,466.00")
    }

    "Scottish user have savings" in {
      val savings = List(
        TaxBand("LSR", "", 32010, 6402, None, None, 20),
        TaxBand("HSR1", "", 36466, 36466, None, None, 40)
      )
      val viewWithSavings: Html = views.html.howIncomeTaxIsCalculated(TaxExplanationViewModel(Seq.empty[TaxBand], savings, Seq.empty[TaxBand], ScottishBands))
      doc(viewWithSavings) must haveTdWithText("32,010")
      doc(viewWithSavings) must haveTdWithText(messages("scottish.bandtype.LSR"))
      doc(viewWithSavings) must haveTdWithText("20%")
      doc(viewWithSavings) must haveTdWithText("6,402.00")
      doc(viewWithSavings) must haveTdWithText("36,466")
      doc(viewWithSavings) must haveTdWithText(messages("scottish.bandtype.HSR1"))
      doc(viewWithSavings) must haveTdWithText("40%")
      doc(viewWithSavings) must haveTdWithText("36,466.00")
    }

    "UK user have dividends" in {
      val dividends = List(
        TaxBand("LDR", "", 32010, 6402, None, None, 20),
        TaxBand("HDR1", "", 36466, 36466, None, None, 40)
      )
      val viewWithSavings: Html = views.html.howIncomeTaxIsCalculated(TaxExplanationViewModel(Seq.empty[TaxBand], Seq.empty[TaxBand], dividends, UkBands))
      doc(viewWithSavings) must haveTdWithText("32,010")
      doc(viewWithSavings) must haveTdWithText(messages("uk.bandtype.LDR"))
      doc(viewWithSavings) must haveTdWithText("20%")
      doc(viewWithSavings) must haveTdWithText("6,402.00")
      doc(viewWithSavings) must haveTdWithText("36,466")
      doc(viewWithSavings) must haveTdWithText(messages("uk.bandtype.HDR1"))
      doc(viewWithSavings) must haveTdWithText("40%")
      doc(viewWithSavings) must haveTdWithText("36,466.00")
    }

    "scottish user have dividends" in {
      val dividends = List(
        TaxBand("LDR", "", 32010, 6402, None, None, 20),
        TaxBand("HDR1", "", 36466, 36466, None, None, 40)
      )
      val viewWithSavings: Html = views.html.howIncomeTaxIsCalculated(TaxExplanationViewModel(Seq.empty[TaxBand], Seq.empty[TaxBand], dividends, UkBands))
      doc(viewWithSavings) must haveTdWithText("32,010")
      doc(viewWithSavings) must haveTdWithText(messages("scottish.bandtype.LDR"))
      doc(viewWithSavings) must haveTdWithText("20%")
      doc(viewWithSavings) must haveTdWithText("6,402.00")
      doc(viewWithSavings) must haveTdWithText("36,466")
      doc(viewWithSavings) must haveTdWithText(messages("scottish.bandtype.HDR1"))
      doc(viewWithSavings) must haveTdWithText("40%")
      doc(viewWithSavings) must haveTdWithText("36,466.00")
    }

  }

  "have additional tax table" in {
//    val additionalRows = Seq(
//      AdditionalTaxDetailRow(Messages("tai.taxCalc.UnderpaymentPreviousYear.title"), 100, None),
//      AdditionalTaxDetailRow(Messages("tai.taxcode.deduction.type-45"), 50, Some(routes.PotentialUnderpaymentController.potentialUnderpaymentPage().url)),
//      AdditionalTaxDetailRow(Messages("tai.taxCalc.OutstandingDebt.title"), 150, None),
//      AdditionalTaxDetailRow(Messages("tai.taxCalc.childBenefit.title"), 300, None),
//      AdditionalTaxDetailRow(Messages("tai.taxCalc.excessGiftAidTax.title"), 100, None),
//      AdditionalTaxDetailRow(Messages("tai.taxCalc.excessWidowsAndOrphans.title"), 100, None),
//      AdditionalTaxDetailRow(Messages("tai.taxCalc.pensionPaymentsAdjustment.title"), 200, None)
//    )
//    val model = createViewModel(true, additionalRows, Seq.empty[ReductionTaxRow])
//    def additionalDetailView: Html = views.html.estimatedIncomeTaxTemp(model, Html("<title/>"))
//
//
//    doc(additionalDetailView).select("#additionalTaxTable").size() mustBe 1
//    doc(additionalDetailView).select("#additionalTaxTable-heading").text mustBe Messages("tai.estimatedIncome.additionalTax.title")
//    doc(additionalDetailView).select("#additionalTaxTable-desc").text() mustBe Messages("tai.estimatedIncome.additionalTax.desc")
//    doc(additionalDetailView).getElementsMatchingOwnText("TaxDescription").hasAttr("data-journey-click") mustBe false
//  }
//
//  "have reduction tax table" in {
//    val  reductionTaxRows = Seq(
//      ReductionTaxRow(Messages("tai.taxCollected.atSource.otherIncome.description"), 100, Messages("tai.taxCollected.atSource.otherIncome.title")),
//      ReductionTaxRow(Messages("tai.taxCollected.atSource.dividends.description", 10), 200, Messages("tai.taxCollected.atSource.dividends.title")),
//      ReductionTaxRow(Messages("tai.taxCollected.atSource.bank.description", 20), 100, Messages("tai.taxCollected.atSource.bank.title"))
//    )
//
//    val model = createViewModel(true, Seq.empty[AdditionalTaxDetailRow], reductionTaxRows)
//    def reductionTaxDetailView: Html = views.html.estimatedIncomeTaxTemp(model, Html("<title/>"))
//
//    doc(reductionTaxDetailView).select("#taxPaidElsewhereTable").size() mustBe 1
//    doc(reductionTaxDetailView).select("#taxPaidElsewhereTable-heading").text() mustBe Messages("tai.estimatedIncome.reductionsTax.title")
//    doc(reductionTaxDetailView).select("#taxPaidElsewhereTable-desc").text() mustBe Messages("tai.estimatedIncome.reductionsTax.desc")
//    doc(reductionTaxDetailView) must haveParagraphWithText(viewModel.incomeTaxReducedToZeroMessage.getOrElse(""))
  }

  val taxAccountSummary = TaxAccountSummary(18573,0,0,0,0)
  val totalTax = TotalTax(0,Seq.empty[IncomeCategory],None,None,None)
  val viewModel = DetailedIncomeTaxEstimateViewModel(totalTax,
    Seq.empty[TaxCodeIncome],taxAccountSummary,Seq.empty[CodingComponent],NonTaxCodeIncome(None,Seq.empty[OtherNonTaxCodeIncome]))

  override def view: Html = views.html.estimatedIncomeTax.detailedIncomeTaxEstimate(viewModel)

  def createViewModel(additionalTaxTable: Seq[AdditionalTaxDetailRow], reductionTaxTable: Seq[ReductionTaxRow]) = {
    DetailedIncomeTaxEstimateViewModel(
      nonSavings = Seq.empty[TaxBand],
      savings = Seq.empty[TaxBand],
      dividends = Seq.empty[TaxBand],
      taxRegion = "",
      incomeTaxEstimate = 900,
      incomeEstimate = 16000,
      taxFreeEstimate = 11500,
      additionalTaxTable,
      reductionTaxTable,
      incomeTaxReducedToZeroMessage = None,
      hasPotentialUnderPayment = false,
      ssrValue = None,
      psrValue = None,
      dividendsMessage = None)
  }

//  nonSavings: Seq[TaxBand],
//  savings: Seq[TaxBand],
//  dividends: Seq[TaxBand],
//  taxRegion: String,
//  incomeTaxEstimate: BigDecimal,
//  incomeEstimate: BigDecimal,
//  taxFreeEstimate: BigDecimal,
//  additionalTaxTable: Seq[AdditionalTaxDetailRow],
//  reductionTaxTable: Seq[ReductionTaxRow],
//  incomeTaxReducedToZeroMessage: Option[String],
//  hasPotentialUnderPayment: Boolean,
//  ssrValue: Option[BigDecimal],
//  psrValue: Option[BigDecimal],
//  dividendsMessage: Option[String]

}