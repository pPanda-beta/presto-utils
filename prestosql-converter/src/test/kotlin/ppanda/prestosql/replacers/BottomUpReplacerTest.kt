package ppanda.prestosql.replacers

import io.kotlintest.shouldBe
import io.kotlintest.specs.AnnotationSpec
import io.mockk.MockKAnnotations
import io.prestosql.sql.SqlFormatter
import io.prestosql.sql.parser.ParsingOptions
import io.prestosql.sql.parser.SqlParser
import io.prestosql.sql.tree.Node
import ppanda.prestosql.converters.ColumnNameBlockQuote
import ppanda.prestosql.converters.TryUnpacker


class BottomUpReplacerTest : AnnotationSpec() {
    private lateinit var bottomUpReplacer: BottomUpReplacer
    private lateinit var replacementStrategy: ReflectionBasedReplacementStrategy<Node, Node>

    @Before
    fun setUp() = run {
        MockKAnnotations.init(this, relaxUnitFun = true)
        bottomUpReplacer = BottomUpReplacer(listOf(TryUnpacker(), ColumnNameBlockQuote()))
        replacementStrategy = ReflectionBasedReplacementStrategy()
    }

    @Test
    fun `should convert presto to hive`() {
        val sql = "CREATE VIEW hive.internal.svo_v3 AS SELECT 123 \"abc\""
        val statement = SqlParser().createStatement(sql, ParsingOptions())
        val modifiedStatement = bottomUpReplacer.replaceAll(statement, replacementStrategy)

        val expectedModifiedSql = """CREATE VIEW hive.internal.svo_v3 AS SELECT 123 `abc`"""

        val modifiedSql = SqlFormatter.formatSql(modifiedStatement)
        modifiedSql shouldMatchIgnoringWhitespaces expectedModifiedSql
    }


    infix fun String.shouldMatchIgnoringWhitespaces(expected: String) =
            replaceWhitespaces(this) shouldBe replaceWhitespaces(expected)

    private fun replaceWhitespaces(expected: String) = expected
            .replace("\\s+".toRegex(), " ")
            .replace("^\\s+|\\s+\$".toRegex(),"")
}