package ppanda.prestosql.analyzer

import io.kotlintest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotlintest.matchers.maps.shouldContain
import io.kotlintest.specs.AnnotationSpec
import io.mockk.MockKAnnotations
import io.prestosql.sql.parser.ParsingOptions
import io.prestosql.sql.parser.SqlParser


class ExplanationTest : AnnotationSpec() {

    @Before
    fun setUp() = MockKAnnotations.init(this, relaxUnitFun = true)

    @Test
    fun `should find lineage info from select query`() {
        val sql = """select x.colA as col1, x.colB as col2 from hive.default.x_table x"""

        val lineageInfo = Explanation.getLineageInfo(SqlParser().createStatement(sql, ParsingOptions()))
        lineageInfo
                .shouldHaveMapping("col1", setOf("\"Table{hive.default.x_table}\".colA"))
                .shouldHaveMapping("col2", setOf("\"Table{hive.default.x_table}\".colB"))
    }

    @Test
    fun `should find lineage info from create view query`() {
        val sql = """create or replace view v1 as select (x.colA * x.colB) as product from hive.default.x_table x"""

        val lineageInfo = Explanation.getLineageInfo(SqlParser().createStatement(sql, ParsingOptions()))

        lineageInfo
                .shouldHaveMapping("product",
                        setOf("\"Table{hive.default.x_table}\".colB", "\"Table{hive.default.x_table}\".colA"))
    }

    @Test
    fun `should find table dependencies of a view`() {
        val sql = """create or replace view v1 as select * from hive.default.x_table inner join hive.default.y_table on id """

        val upstreamTables = Explanation.getDependencyTables(SqlParser().createStatement(sql, ParsingOptions()))

        upstreamTables
                .map { it.toString() }
                .shouldContainExactlyInAnyOrder("Table{hive.default.y_table}", "Table{hive.default.x_table}")
    }

    private fun <K : Any, V : Any> Map<K, Collection<V>>.shouldHaveMapping(keyString: String, values: Set<String>)
            : Map<K, Collection<V>> {
        this
                .mapKeys { it.key.toString() }
                .mapValues { it.value.map(Any::toString).toSet() }
                .shouldContain(keyString, values)
        return this
    }
}