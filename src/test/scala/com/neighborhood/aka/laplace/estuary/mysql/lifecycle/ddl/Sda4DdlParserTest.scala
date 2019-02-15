package com.neighborhood.aka.laplace.estuary.mysql.lifecycle.ddl

import com.neighborhood.aka.laplace.estuary.UnitSpec
import com.neighborhood.aka.laplace.estuary.mysql.schema.defs.ddl.{AddColumnMod, TableAlter}
import com.neighborhood.aka.laplace.estuary.mysql.schema.{Parser, SdaSchemaMappingRule}
import com.neighborhood.aka.laplace.estuary.mysql.schema.Parser.SchemaChangeToDdlSqlSyntax

/**
  * Created by john_liu on 2019/2/14.
  */
class Sda4DdlParserTest extends UnitSpec {

  val mappingRuleMap = Map("a.a" -> "a_map.a_map", "b.b" -> "b_map.b_map")
  val schemaMappingRule = new SdaSchemaMappingRule(mappingRuleMap)
  val alterTable1 = "ALTER TABLE a.a ADD col1 text DEFAULT 'hello';"
  val alterTable2 = "ALTER TABLE `a`.`a` ADD column `col1` int(11) comment 'c' AFTER `afterCol`"

  "test 1" should "successfully handle Alter table with column add" in {
    val schemaChange = Parser.parseAndReplace(alterTable1, "a_map", schemaMappingRule)
    assert(schemaChange.isInstanceOf[TableAlter])
    val tableAlter = schemaChange.asInstanceOf[TableAlter]
    assert(tableAlter.database == "a_map")
    assert(tableAlter.table == "a_map")
    assert(tableAlter.newDatabase == "a_map")
    assert(tableAlter.newTableName == "a_map")
    val addColumnMod = tableAlter.columnMods.get(0).asInstanceOf[AddColumnMod]
    assert(addColumnMod.definition.getName == "col1")
    assert(addColumnMod.definition.getType == "text")
    assert(addColumnMod.definition.getDefaultValue == "'hello'")
    val ddl = schemaChange.toDdlSql
    assert(ddl == "ALTER TABLE a_map.a_map ADD col1 text  DEFAULT 'hello'")
  }

  "test 2" should "successfully handle Alter table with column add" in {
    val schemaChange = Parser.parseAndReplace(alterTable2, "a_map", schemaMappingRule)
    assert(schemaChange.isInstanceOf[TableAlter])
    val tableAlter = schemaChange.asInstanceOf[TableAlter]
    assert(tableAlter.database == "a_map")
    assert(tableAlter.table == "a_map")
    assert(tableAlter.newDatabase == "a_map")
    assert(tableAlter.newTableName == "a_map")
    val addColumnMod = tableAlter.columnMods.get(0).asInstanceOf[AddColumnMod]
    assert(addColumnMod.definition.getName == "col1")
    assert(addColumnMod.definition.getType == "int")
    assert(addColumnMod.definition.getDefaultValue == null)
    assert(addColumnMod.definition.getComment == "c")
    val ddl = schemaChange.toDdlSql
    assert(ddl.trim == "ALTER TABLE a_map.a_map ADD COLUMN col1 int")
  }
}
