package ppanda.prestosql.hive.converter

import io.prestosql.sql.SqlFormatter
import io.prestosql.sql.parser.ParsingOptions
import io.prestosql.sql.parser.SqlParser
import io.prestosql.sql.tree.Node
import io.prestosql.sql.tree.Statement
import org.apache.hadoop.hive.ql.parse.ASTNode
import org.apache.hadoop.hive.ql.parse.ParseDriver
import ppanda.prestosql.converters.ColumnNameBlockQuote
import ppanda.prestosql.converters.SqlConverter
import ppanda.prestosql.converters.TryUnpacker
import ppanda.prestosql.replacers.BottomUpReplacer
import ppanda.prestosql.replacers.ReflectionBasedReplacementStrategy


open class PrestoToHive(
        converters: List<SqlConverter<out Node>> = listOf(TryUnpacker(), ColumnNameBlockQuote()),
        val parsingOptions: ParsingOptions = ParsingOptions(),
        val prestosqlParser: SqlParser = SqlParser(),
        val hiveParserDriver: ParseDriver = ParseDriver()
) {
    private val bottomUpReplacer = BottomUpReplacer(converters)
    private val replacementStrategy = ReflectionBasedReplacementStrategy<Node, Node>()

    open fun convertStatement(prestoSql: String) =
            convert(prestoSql, prestosqlParser.createStatement(prestoSql, parsingOptions))

    open fun convert(originalPrestoSql: String, prestoStatement: Statement): ConversionResult {
        var convertedPrestoSql: Statement? = null
        var convertedHiveql: String? = null
        var convertedParsedHiveql: ASTNode? = null
        var error: Exception? = null

        try {
            convertedPrestoSql = bottomUpReplacer.replaceAll(prestoStatement, replacementStrategy)
            convertedHiveql = SqlFormatter.formatSql(convertedPrestoSql)
            convertedParsedHiveql = hiveParserDriver.parse(convertedHiveql)
        } catch (e: Exception) {
            println(e)
            error = e
        }


        return ConversionResult(
                originalPrestoSql = originalPrestoSql,
                parsedPrestoSql = prestoStatement,
                convertedHiveql = convertedHiveql,
                convertedParsedHiveql = convertedParsedHiveql,
                error = error
        )
    }
}


data class ConversionResult(
        val originalPrestoSql: String,
        val parsedPrestoSql: Statement?,
        val convertedHiveql: String?,
        val convertedParsedHiveql: ASTNode?,
        val error: Exception?
) {
    fun isSuccessful(): Boolean = convertedHiveql != null
    fun hasValidHql(): Boolean = convertedParsedHiveql != null
}