package ppanda.prestosql.hive.converter

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.MockKAnnotations


class PrestoToHiveTest : AnnotationSpec() {
    private lateinit var prestoToHive: PrestoToHive

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        this.prestoToHive = PrestoToHive()
    }

    @Test
    fun `should convert presto to hive`() {
        val sql = "CREATE VIEW internal.svo_v3 AS SELECT 123 AS \"abc\""

        val result = prestoToHive.convertStatement(sql)
        val expectedModifiedSql = """CREATE VIEW internal.svo_v3 AS SELECT 123 `abc` """
        result.convertedHiveql!! shouldMatchIgnoringWhitespaces expectedModifiedSql
    }

    @Test
    fun `should remove catalog from table name`() {
        val sql = "CREATE VIEW internal.svo_v3 AS SELECT * FROM hive.my_schema.tab1 "

        val result = prestoToHive.convertStatement(sql)
        val expectedModifiedSql = """CREATE VIEW internal.svo_v3 AS SELECT * FROM my_schema.tab1 """
        result.convertedHiveql!! shouldMatchIgnoringWhitespaces expectedModifiedSql
    }

    @Test
    fun `should modify syntax for array construction `() {
        val sql = "SELECT array[1, 2, 3]"

        val result = prestoToHive.convertStatement(sql)
        val expectedModifiedSql = """SELECT array(1, 2, 3)"""
        result.convertedHiveql!! shouldMatchIgnoringWhitespaces expectedModifiedSql
    }


    @Test
    fun `should convert functions`() {
        val sql = "SELECT json_extract(json, '\$.store.book') as book FROM hive.my_schema.tab1 "

        val result = prestoToHive.convertStatement(sql)
        val expectedModifiedSql = """SELECT get_json_object(json, '$.store.book') `book` FROM my_schema.tab1"""
        result.convertedHiveql!! shouldMatchIgnoringWhitespaces expectedModifiedSql
    }

    @Test
    fun `should convert data types`() {
        val sql = "SELECT CAST(col1 as VARCHAR) FROM hive.my_schema.tab1 "

        val result = prestoToHive.convertStatement(sql)
        val expectedModifiedSql = """SELECT CAST(col1 AS STRING) FROM my_schema.tab1 """
        result.convertedHiveql!! shouldMatchIgnoringWhitespaces expectedModifiedSql
    }

    @Test
    fun `should modify unnest without cardinality `() {
        //TODO: Hive mandates you to put a table alias -> SELECT * FROM (...) AS `myDynamicallyGeneratedTableAlias`
        val sql = """SELECT * FROM unnest(array[1,2,3]) as t(x)"""

        val result = prestoToHive.convertStatement(sql)
        val expectedModifiedSql = """SELECT * FROM ( SELECT explode(array(1, 2, 3)) x )"""
        result.convertedHiveql!! shouldMatchIgnoringWhitespaces expectedModifiedSql
    }


    infix fun String.shouldMatchIgnoringWhitespaces(expected: String) =
            replaceWhitespaces(this) shouldBe replaceWhitespaces(expected)

    private fun replaceWhitespaces(expected: String) = expected
            .replace("\\s+".toRegex(), " ")
            .replace("^\\s+|\\s+\$".toRegex(), "")
}