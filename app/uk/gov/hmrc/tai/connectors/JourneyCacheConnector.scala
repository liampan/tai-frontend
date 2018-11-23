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

package uk.gov.hmrc.tai.connectors

import play.api.http.Status._
import play.api.libs.json.Reads
import uk.gov.hmrc.http._
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.tai.config.WSHttp
import uk.gov.hmrc.tai.connectors.responses.{TaiResponse, TaiSuccessResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait JourneyCacheConnector {

  val serviceUrl: String
  val httpHandler: HttpHandler
  val httpClient: WSHttp = WSHttp

  def cacheUrl(journeyName: String) = s"$serviceUrl/tai/journey-cache/$journeyName"

  def currentCache(journeyName: String)(implicit hc: HeaderCarrier): Future[Map[String,String]] = {
    httpHandler.getFromApi(cacheUrl(journeyName)).map(_.as[Map[String,String]]) recover {
      case e:NotFoundException => Map.empty[String, String]
    }
  }

  def currentValueAs[T](journeyName: String, key: String, convert: String => T)(implicit hc: HeaderCarrier): Future[Option[T]] = {
    val url = s"${cacheUrl(journeyName)}/values/$key"
    httpHandler.getFromApi(url).map(value => Some(convert(value.as[String]))) recover {
      case e:NotFoundException => None
    }
  }

  def mandatoryValueAs[T](journeyName: String, key: String, convert: String => T)(implicit hc: HeaderCarrier): Future[T] = {
    val url = s"${cacheUrl(journeyName)}/values/$key"
    httpHandler.getFromApi(url).map(value => convert(value.as[String])) recover {
      case e:NotFoundException => throw new RuntimeException(s"The mandatory value under key '$key' was not found in the journey cache for '$journeyName'")
    }
  }

  def cache(journeyName: String, data: Map[String,String])(implicit hc: HeaderCarrier): Future[Map[String,String]] = {
    httpHandler.postToApi(
      cacheUrl(journeyName), data
    ).map(_.json.as[Map[String,String]])
  }

  def flush(journeyName: String)(implicit hc: HeaderCarrier): Future[TaiResponse] = {
    httpHandler.deleteFromApi(cacheUrl(journeyName)).map(_ => TaiSuccessResponse)
  }

  def getCache[T](journeyName: String)(implicit hc: HeaderCarrier, reads: Reads[T]): Future[Option[T]] = {
    httpClient.GET[HttpResponse](cacheUrl(journeyName)).map(httpResponse =>
          httpResponse.status match {
            case OK => Some(httpResponse.json.as[T])
          }
    ).recover {
      case x: NotFoundException => None
      case _ => throw new RuntimeException(s"[JourneyCacheConnector] - Server error received for $journeyName")
    }
  }
}

// $COVERAGE-OFF$
object JourneyCacheConnector extends JourneyCacheConnector with ServicesConfig{
  override val serviceUrl = baseUrl("tai")
  val httpHandler: HttpHandler = HttpHandler
}
// $COVERAGE-ON$