package org.docksidestage.handson.exercise;
import org.docksidestage.handson.unit.UnitContainerTestCase;

public class HandsOn02Test extends UnitContainerTestCase {
}

// #1on1: section2のReplaceSchemaのエラー
// 起きた事象としては、NotNullのカラムのデータが指定されてなかった (かつ、デフォルトの制約もない)
// 一方で、解決方法としては、色々考えられる。
// 選択肢A. デフォルト制約を付ける (DDLの修正: DB変更)
// 選択肢B. TSVにデータを定義する (TSVの修正: テストデータの変更)
// 選択肢C. DBFluteのデフォルト機能を使って解決する
//
// (個人的にはデフォルト制約は便宜上必要になったときだけ使う感覚、業務ロジックとも言えるのでDBに入れたくない)
// (あと、insertでカラム指定を忘れてしまったとき、変にデフォルト値で動くよりもエラーで落ちて欲しい)
//
// (共通カラムは、本番では重要だけど、テストではそこまでではないので、一律のデフォルトで解決したくなる)
// (デフォルト制約は使いたくないけど、ここだけピンポイントでデフォルト解決したい)
// (なので、DBFluteのdefaultValueMap.dataprop: ReplaceSchemaのときだけデフォルト値を使う機能)
//
// (あと、MySQL例外翻訳してますか？話)
//
//[df-replace-schema] 2025-11-14 14:39:17,683 ERROR - Look! Read the message below.
//[df-replace-schema] /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//[df-replace-schema] Failed to execute DBFlute Task 'ReplaceSchema'.
//[df-replace-schema] 
//[df-replace-schema] [Advice]
//[df-replace-schema] Check the exception messages and the stack traces.
//[df-replace-schema] 
//[df-replace-schema] [Database Product]
//[df-replace-schema] MySQL 8.0.36
//[df-replace-schema] 
//[df-replace-schema] [JDBC Driver]
//[df-replace-schema] MySQL Connector Java mysql-connector-java-5.1.49 ( Revision: ad86f36e100e104cd926c6b81c8cab9565750116 ) for JDBC 4.0
//[df-replace-schema] * * * * * * * * * */
//[df-replace-schema] org.dbflute.exception.DfDelimiterDataRegistrationFailureException: Look! Read the message below.
//[df-replace-schema] /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
//[df-replace-schema] Failed to register the table data.
//[df-replace-schema] 
//[df-replace-schema] [Advice]
//[df-replace-schema] Please confirm the SQLException message.
//[df-replace-schema] *And also check the insert values to not-null columns.
//[df-replace-schema] 
//[df-replace-schema] [Delimiter File]
//[df-replace-schema] playsql/data/ut/reversetsv/UTF-8/cyclic_01_01-PRODUCT.tsv
//[df-replace-schema] 
//[df-replace-schema] [Table]
//[df-replace-schema] PRODUCT
//[df-replace-schema] 
//[df-replace-schema] [SQLException]
//[df-replace-schema] org.dbflute.exception.DfJDBCException
//[df-replace-schema] JDBC said...
//[df-replace-schema] /- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
//[df-replace-schema] [SQLException]
//[df-replace-schema] java.sql.BatchUpdateException
//[df-replace-schema] Field 'REGISTER_DATETIME' doesn't have a default value
//[df-replace-schema] - - - - - - - - - -/
//[df-replace-schema] 
//[df-replace-schema] [Target Row]
//[df-replace-schema] (derived from non-batch retry)
//[df-replace-schema] java.sql.SQLException
//[df-replace-schema] Field 'REGISTER_DATETIME' doesn't have a default value
//[df-replace-schema] /- - - - - - - - - - - - - - - - - - - -
//[df-replace-schema] Column Def: [PRODUCT_ID, PRODUCT_NAME, PRODUCT_HANDLE_CODE, PRODUCT_CATEGORY_CODE, PRODUCT_STATUS_CODE, REGULAR_PRICE, VERSION_NO]
//[df-replace-schema] Row Values: [1, Cold Spring Harbor, BILLYJOEL-01, MCD, PST, 1100, 0]
//[df-replace-schema] Row Number: 1
//[df-replace-schema] - - - - - - - - - -/
//[df-replace-schema] 
//[df-replace-schema] [Executed SQL]
//[df-replace-schema] insert into PRODUCT (PRODUCT_ID, PRODUCT_NAME, PRODUCT_HANDLE_CODE, PRODUCT_CATEGORY_CODE, PRODUCT_STATUS_CODE, REGULAR_PRICE, VERSION_NO) values(?, ?, ?, ?, ?, ?, ?)
//[df-replace-schema] 
//[df-replace-schema] [Bind Type]
//[df-replace-schema] PRODUCT_ID = class java.lang.Integer
//[df-replace-schema] PRODUCT_NAME = class java.lang.String
//[df-replace-schema] PRODUCT_HANDLE_CODE = class java.lang.String
//[df-replace-schema] PRODUCT_CATEGORY_CODE = class java.lang.String
//[df-replace-schema] PRODUCT_STATUS_CODE = class java.lang.String
//[df-replace-schema] REGULAR_PRICE = class java.lang.Integer
//[df-replace-schema] VERSION_NO = class java.lang.Long
//[df-replace-schema] 
//[df-replace-schema] [String Processor]
//[df-replace-schema] PRODUCT_ID = NumberStringProcessor
//[df-replace-schema] PRODUCT_NAME = RealStringProcessor
//[df-replace-schema] PRODUCT_HANDLE_CODE = RealStringProcessor
//[df-replace-schema] PRODUCT_CATEGORY_CODE = RealStringProcessor
//[df-replace-schema] PRODUCT_STATUS_CODE = RealStringProcessor
//[df-replace-schema] REGULAR_PRICE = NumberStringProcessor
//[df-replace-schema] VERSION_NO = NumberStringProcessor
//[df-replace-schema] * * * * * * * * * */
//[df-replace-schema]     at org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataWriterImpl.doWriteData(DfDelimiterDataWriterImpl.java:359)
//[df-replace-schema]     at org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataWriterImpl.writeData(DfDelimiterDataWriterImpl.java:101)
//[df-replace-schema]     at org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataHandlerImpl.writeSeveralData(DfDelimiterDataHandlerImpl.java:127)
//[df-replace-schema]     at org.dbflute.logic.replaceschema.process.DfLoadDataProcess.writeDbFromDelimiterFile(DfLoadDataProcess.java:200)
//[df-replace-schema]     at org.dbflute.logic.replaceschema.process.DfLoadDataProcess.writeDbFromDelimiterFileAsLoadingTypeData(DfLoadDataProcess.java:190)
//[df-replace-schema]     at org.dbflute.logic.replaceschema.process.DfLoadDataProcess.execute(DfLoadDataProcess.java:161)
//[df-replace-schema]     at org.dbflute.task.DfReplaceSchemaTask.loadData(DfReplaceSchemaTask.java:361)
//[df-replace-schema]     at org.dbflute.task.DfReplaceSchemaTask.executeCoreProcess(DfReplaceSchemaTask.java:259)
//[df-replace-schema]     at org.dbflute.task.DfReplaceSchemaTask.processReplaceSchema(DfReplaceSchemaTask.java:240)
//[df-replace-schema]     at org.dbflute.task.DfReplaceSchemaTask.doExecute(DfReplaceSchemaTask.java:160)
//[df-replace-schema]     at org.dbflute.task.bs.DfAbstractTexenTask$1.callActualExecute(DfAbstractTexenTask.java:129)
//[df-replace-schema]     at org.dbflute.task.bs.assistant.DfTaskBasicController.doExecute(DfTaskBasicController.java:192)
//[df-replace-schema]     at org.dbflute.task.bs.assistant.DfTaskBasicController.execute(DfTaskBasicController.java:78)
//[df-replace-schema]     at org.dbflute.task.bs.DfAbstractTexenTask.execute(DfAbstractTexenTask.java:151)
//[df-replace-schema]     at org.apache.tools.ant.UnknownElement.execute(UnknownElement.java:288)
//[df-replace-schema]     at sun.reflect.GeneratedMethodAccessor3.invoke(Unknown Source)
//[df-replace-schema]     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
//[df-replace-schema]     at java.lang.reflect.Method.invoke(Method.java:498)
//[df-replace-schema]     at org.apache.tools.ant.dispatch.DispatchUtils.execute(DispatchUtils.java:105)
//[df-replace-schema]     at org.apache.tools.ant.Task.perform(Task.java:348)
//[df-replace-schema]     at org.apache.tools.ant.Target.execute(Target.java:357)
//[df-replace-schema]     at org.apache.tools.ant.Target.performTasks(Target.java:385)
//[df-replace-schema]     at org.apache.tools.ant.Project.executeSortedTargets(Project.java:1329)
//[df-replace-schema]     at org.apache.tools.ant.Project.executeTarget(Project.java:1298)
//[df-replace-schema]     at org.apache.tools.ant.helper.DefaultExecutor.executeTargets(DefaultExecutor.java:41)
//[df-replace-schema]     at org.apache.tools.ant.Project.executeTargets(Project.java:1181)
//[df-replace-schema]     at org.apache.tools.ant.Main.runBuild(Main.java:698)
//[df-replace-schema]     at org.apache.tools.ant.Main.startAnt(Main.java:199)
//[df-replace-schema]     at org.apache.tools.ant.launch.Launcher.run(Launcher.java:257)
//[df-replace-schema]     at org.apache.tools.ant.launch.Launcher.main(Launcher.java:104)
//[df-replace-schema] Caused by: java.sql.BatchUpdateException: Field 'REGISTER_DATETIME' doesn't have a default value
//[df-replace-schema]     at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
//[df-replace-schema]     at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
//[df-replace-schema]     at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
//[df-replace-schema]     at java.lang.reflect.Constructor.newInstance(Constructor.java:423)
//[df-replace-schema]     at com.mysql.jdbc.Util.handleNewInstance(Util.java:403)
//[df-replace-schema]     at com.mysql.jdbc.Util.getInstance(Util.java:386)
//[df-replace-schema]     at com.mysql.jdbc.SQLError.createBatchUpdateException(SQLError.java:1154)
//[df-replace-schema]     at com.mysql.jdbc.PreparedStatement.executeBatchSerially(PreparedStatement.java:1835)
//[df-replace-schema]     at com.mysql.jdbc.PreparedStatement.executeBatchInternal(PreparedStatement.java:1319)
//[df-replace-schema]     at com.mysql.jdbc.StatementImpl.executeBatch(StatementImpl.java:954)
//[df-replace-schema]     at org.dbflute.logic.replaceschema.loaddata.delimiter.DfDelimiterDataWriterImpl.doWriteData(DfDelimiterDataWriterImpl.java:346)
//[df-replace-schema]     ... 29 more
//[df-replace-schema] Caused by: java.sql.SQLException: Field 'REGISTER_DATETIME' doesn't have a default value
//[df-replace-schema]     at com.mysql.jdbc.SQLError.createSQLException(SQLError.java:965)
//[df-replace-schema]     at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:3933)
//[df-replace-schema]     at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:3869)
//[df-replace-schema]     at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:2524)
//[df-replace-schema]     at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2675)
//[df-replace-schema]     at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2465)
//[df-replace-schema]     at com.mysql.jdbc.PreparedStatement.executeInternal(PreparedStatement.java:1915)
//[df-replace-schema]     at com.mysql.jdbc.PreparedStatement.executeUpdateInternal(PreparedStatement.java:2136)
//[df-replace-schema]     at com.mysql.jdbc.PreparedStatement.executeBatchSerially(PreparedStatement.java:1813)
//[df-replace-schema]     ... 32 more
//[df-replace-schema] 2025-11-14 14:39:17,692 INFO  - ...commit()
//[df-replace-schema] 2025-11-14 14:39:17,695 INFO  - ...rollback()
//[df-replace-schema] 2025-11-14 14:39:17,698 INFO  - ...closeReally()
//[df-replace-schema] 2025-11-14 14:39:17,707 INFO  - 
//[df-replace-schema] _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
//[df-replace-schema] [Final Message]: 00m01s904ms *Abort
//[df-replace-schema] 
//[df-replace-schema]   DBFLUTE_CLIENT: {maihamadb}
//[df-replace-schema]     database  = mysql (MySQL 8.0.36)
//[df-replace-schema]     language  = java
//[df-replace-schema]     container = lasta_di
//[df-replace-schema]     package   = org.docksidestage.handson.dbflute
//[df-replace-schema] 
//[df-replace-schema]   DBFLUTE_ENVIRONMENT_TYPE: {}
//[df-replace-schema]     driver = com.mysql.jdbc.Driver
//[df-replace-schema]     url    = jdbc:mysql://localhost:43376/maihamadb?allowPublicKeyRetrieval=true&sslMode=DISABLED
//[df-replace-schema]     schema = {maihamadb.$$NoNameSchema$$ as main}
//[df-replace-schema]     user   = maihamadb
//[df-replace-schema]     props  = {user=root, password=}
//[df-replace-schema]     additionalSchema = 
//[df-replace-schema]     repsEnvType      = ut
//[df-replace-schema]     refreshProject   = $$AutoDetect$$
//[df-replace-schema] 
//[df-replace-schema]  Create Schema: success=42, failure=0 (in 2 files) - 00m00s775ms
//[df-replace-schema]   (Initialize Schema) - 00m00s319ms
//[df-replace-schema]   (Create Schema) - 00m00s450ms
//[df-replace-schema]   o replace-schema-00-system.sql
//[df-replace-schema]   o replace-schema-10-basic.sql
//[df-replace-schema] 
//[df-replace-schema]  Load Data: common:{tsv=6} - 00m00s211ms
//[df-replace-schema]   <common>
//[df-replace-schema]   o acyclic_01_01-MEMBER_STATUS.tsv
//[df-replace-schema]   o acyclic_01_02-PRODUCT_CATEGORY.tsv
//[df-replace-schema]   o acyclic_01_03-PRODUCT_STATUS.tsv
//[df-replace-schema]   o acyclic_01_04-REGION.tsv
//[df-replace-schema]   o acyclic_01_05-SERVICE_RANK.tsv
//[df-replace-schema]   o acyclic_01_06-WITHDRAWAL_REASON.tsv
//[df-replace-schema]   x (failed: Look at the exception message)
//[df-replace-schema]     * * * * * *
//[df-replace-schema]     * Failure *
//[df-replace-schema]     * * * * * *
//[df-replace-schema] _/_/_/_/_/_/_/_/_/_/ {ReplaceSchema}
//
//BUILD FAILED
