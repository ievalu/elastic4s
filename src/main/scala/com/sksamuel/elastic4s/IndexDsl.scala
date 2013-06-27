package com.sksamuel.elastic4s

import org.elasticsearch.index.VersionType
import org.elasticsearch.action.index.IndexRequest.OpType
import org.elasticsearch.common.xcontent.{XContentFactory, XContentBuilder}
import org.elasticsearch.action.index.{IndexResponse, IndexRequest}
import scala.collection.mutable.ListBuffer
import scala.concurrent.Future

/** @author Stephen Samuel */
object IndexDsl {

    def insert: IndexExpectsInto = new IndexExpectsInto
    def index: IndexExpectsInto = new IndexExpectsInto

    class IndexExpectsInto {
        def into(index: String): IndexBuilder = into(index.split("/").toSeq)
        private def into(seq: Seq[String]): IndexBuilder = into(seq(0), seq(1))
        def into(index: String, `type`: String): IndexBuilder = new IndexBuilder(index, `type`)
        def into(kv: (String, String)): IndexBuilder = into(kv._1, kv._2)
    }

    class IndexBuilder(index: String, `type`: String) {

        private val _request = new IndexRequest(index, `type`)
        private val _fields = new ListBuffer[(String, Any)]

        def id(id: Any) = {
            _request.id(id.toString)
            this
        }
        def routing(routing: String) = {
            _request.routing(routing)
            this
        }

        def parent(parent: String) = {
            _request.parent(parent)
            this
        }

        def timestamp(timestamp: String) = {
            _request.timestamp(timestamp)
            this
        }

        def ttl(ttl: Long) = {
            _request.ttl(ttl)
            this
        }

        def update(update: Boolean) = opType(OpType.CREATE)
        def opType(opType: IndexRequest.OpType) = {
            _request.opType(opType)
            this
        }

        def version(version: Int) = {
            _request.version(version)
            this
        }

        def versionType(versionType: VersionType) = {
            _request.versionType(versionType)
            this
        }

        def fields(fields: (String, Any)*) = {
            this._fields ++= fields
            this
        }

        def fields(map: Map[String, Any]) = {
            _fields ++= map.toList
            this
        }

        def _source: XContentBuilder = {
            val source = XContentFactory.jsonBuilder().startObject()
            for ( tuple <- _fields ) {
                source.field(tuple._1, tuple._2)
            }
            source.endObject()
        }

        def execute(implicit client: ElasticClient): Future[IndexResponse] = client.execute(this)

        def java = _request.source(_source)
    }
}
