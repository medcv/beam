/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.beam.sdk.extensions.sql.impl.rel;

import org.apache.beam.sdk.extensions.sql.TestUtils;
import org.apache.beam.sdk.extensions.sql.mock.MockedBoundedTable;
import org.apache.beam.sdk.schemas.Schema.TypeName;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test for {@code BeamValuesRel}.
 */
public class BeamValuesRelTest extends BaseRelTest {
  @Rule
  public final TestPipeline pipeline = TestPipeline.create();

  @BeforeClass
  public static void prepare() {
    registerTable("string_table",
        MockedBoundedTable.of(
            TypeName.STRING, "name",
            TypeName.STRING, "description"
        )
    );
    registerTable("int_table",
        MockedBoundedTable.of(
            TypeName.INT32, "c0",
            TypeName.INT32, "c1"
        )
    );
  }

  @Test
  public void testValues() throws Exception {
    String sql = "insert into string_table(name, description) values "
        + "('hello', 'world'), ('james', 'bond')";
    PCollection<Row> rows = compilePipeline(sql, pipeline);
    PAssert.that(rows).containsInAnyOrder(
        TestUtils.RowsBuilder.of(
            TypeName.STRING, "name",
            TypeName.STRING, "description"
        ).addRows(
            "hello", "world",
            "james", "bond"
        ).getRows()
    );
    pipeline.run();
  }

  @Test
  public void testValues_castInt() throws Exception {
    String sql = "insert into int_table (c0, c1) values(cast(1 as int), cast(2 as int))";
    PCollection<Row> rows = compilePipeline(sql, pipeline);
    PAssert.that(rows).containsInAnyOrder(
        TestUtils.RowsBuilder.of(
            TypeName.INT32, "c0",
            TypeName.INT32, "c1"
        ).addRows(
            1, 2
        ).getRows()
    );
    pipeline.run();
  }

  @Test
  public void testValues_onlySelect() throws Exception {
    String sql = "select 1, '1'";
    PCollection<Row> rows = compilePipeline(sql, pipeline);
    PAssert.that(rows).containsInAnyOrder(
        TestUtils.RowsBuilder.of(
            TypeName.INT32, "EXPR$0",
            TypeName.STRING, "EXPR$1"
        ).addRows(
            1, "1"
        ).getRows()
    );
    pipeline.run();
  }
}
