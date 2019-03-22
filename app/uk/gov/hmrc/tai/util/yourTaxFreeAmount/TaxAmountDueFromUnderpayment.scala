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

package uk.gov.hmrc.tai.util.yourTaxFreeAmount

import uk.gov.hmrc.tai.model.domain.tax.{NonSavingsIncomeCategory, TotalTax}
import uk.gov.hmrc.tai.util.constants.BandTypesConstants

object TaxAmountDueFromUnderpayment extends BandTypesConstants {

  def amountDue(totalTaxDue: BigDecimal, totalTax: TotalTax): BigDecimal = {
    val incomeCategories = totalTax.incomeCategories.filter(_.incomeCategoryType == NonSavingsIncomeCategory)
    val taxBand = incomeCategories.flatMap(_.taxBands).find(_.bandType == BasicRate)
    val taxRate: BigDecimal = taxBand.map(_.rate / 100) match {
      case Some(x) => x
      case _ => throw new RuntimeException("Failed to calculate the tax amount due")
    }

    totalTaxDue * taxRate
  }
}
