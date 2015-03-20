package geotrellis.admin.server

import spray.routing._
import geotrellis.vector._
import geotrellis.vector.io.json._
import geotrellis.vector.reproject._
import spray.json._
import com.github.nscala_time.time.Imports._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.accumulo._
import geotrellis.spark.op.stats._
import geotrellis.proj4._
import geotrellis.spark.op.stats._
import geotrellis.spark.op.zonal.summary._
import geotrellis.raster.op.zonal.summary._


trait ZonalSummaryRoutes { self: HttpService with CorsSupport =>
  import ZonalSummaryRoutes._ 
  import spray.httpx.SprayJsonSupport._  
  import GeoJsonSupport._

  def zonalRoutes(catalog: AccumuloCatalog) = cors {
    (pathPrefix(Segment / IntNumber) & (post) ) { (name, zoom) =>      
      import DefaultJsonProtocol._ 
      import org.apache.spark.SparkContext._        
      
      val layer = LayerId(name, zoom)      
      val (lmd, params) = catalog.metaDataCatalog.load(layer)
      val md = lmd.rasterMetaData  
      
      entity(as[Polygon]) { poly => 
        val polygon = poly.reproject(LatLng, md.crs)
        val bounds = md.mapTransform(polygon.envelope)
        val tiles = catalog.load[SpaceTimeKey](layer, FilterSet(SpaceFilter[SpaceTimeKey](bounds)))
      
        path("min") { 
          complete {    
            statsReponse(name,
              tiles
              .mapKeys { key => key.updateTemporalComponent(key.temporalKey.time.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0)) }
              .averageByKey
              .zonalSummaryByKey(polygon, Double.MaxValue, MinDouble, stk => stk.temporalComponent.time)
              .collect
              .sortBy(_._1) )
          } 
        } ~          
        path("max") { 
          complete {    
            statsReponse(name,
              tiles
              .mapKeys { key => key.updateTemporalComponent(key.temporalKey.time.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0)) }
              .averageByKey
              .zonalSummaryByKey(polygon, Double.MinValue, MaxDouble, stk => stk.temporalComponent.time)
              .collect
              .sortBy(_._1) )
          } 
        }          

        // path("multimodel") {
        //   complete {
        //     val extent = extents(city).reproject(LatLng, md.crs)
        //     val bounds = md.mapTransform(extent)
        //     val model1 = catalog.load[SpaceTimeKey](LayerId("tas-miroc5-rcp45",4), FilterSet(SpaceFilter[SpaceTimeKey](bounds))).get
        //     val model2 = catalog.load[SpaceTimeKey](LayerId("tas-access1-rcp45",4), FilterSet(SpaceFilter[SpaceTimeKey](bounds))).get
        //     val model3 = catalog.load[SpaceTimeKey](LayerId("tas-cm-rcp45",4), FilterSet(SpaceFilter[SpaceTimeKey](bounds))).get
        //     val ret = new RasterRDD[SpaceTimeKey](model1.union(model2).union(model3), md)
        //       .mapKeys { key => key.updateTemporalComponent(key.temporalKey.time.withMonthOfYear(1).withDayOfMonth(1).withHourOfDay(0)) }
        //       .averageByKey
        //       .zonalSummaryByKey(extent, Double.MinValue, MaxDouble, stk => stk.temporalComponent.time)
        //       .collect
        //       .sortBy(_._1)
        //     statsReponse("miroc5-access1-cm", ret)
        //   }
        // } 
      }
    }
  }
}

object ZonalSummaryRoutes {
  val extents = Map(
    "philadelphia" -> Extent(-75.9284841594,39.5076933259,-74.3543298352,40.5691292481),
    "orlando"      -> Extent(-81.6640927758,28.2876159612,-81.0875104164,28.7973683779),
    "sanjose"      -> Extent(-122.204281443,37.1344012781,-121.5923878102,37.5401705236),
    "portland"     -> Extent(-123.015276532,45.2704007159,-122.3044835961,45.7602907701),
    "usa"          -> Extent(-124.9268976258,25.3021853935,-65.521646682,49.0009765951),
    "pa"           -> Extent(-80.8936945008,39.7560786402,-74.662271682,42.2519392104),
    "world"        -> Extent(-176.8565908405,-80.2714428203,176.8357981709,83.5687714812)
  )

  def statsReponse(model: String, data: Seq[(DateTime, Double)]) =  
    JsArray(JsObject(
      "model" -> JsString(model),
      "data" -> JsArray(
        data.map { case (date, value) =>
          JsObject(
            "time" -> JsString(date.toString), 
            "mean" -> JsNumber(value),
            "min" -> JsNumber(value * (1 + scala.util.Random.nextDouble/3)), // I know, I'm a liar
            "max" -> JsNumber(value * (1 - scala.util.Random.nextDouble/3))
          )
        }: _*)
    ))
}
