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


    infix fun String.shouldMatchIgnoringWhitespaces(expected: String) =
            replaceWhitespaces(this) shouldBe replaceWhitespaces(expected)

    private fun replaceWhitespaces(expected: String) = expected
            .replace("\\s+".toRegex(), " ")
            .replace("^\\s+|\\s+\$".toRegex(), "")
}