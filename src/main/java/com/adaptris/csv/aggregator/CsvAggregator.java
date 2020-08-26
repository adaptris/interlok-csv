/*******************************************************************************
 * Copyright 2020 Adaptris Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.adaptris.csv.aggregator;

import com.adaptris.annotation.ComponentProfile;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Attempts to aggregate messages into a CSV file.
 *
 * <p>
 * Note that this is not a validating CSV aggregator since it does not check that the data in the messages to be joined are in fact
 * CSV documents (or in fact match). If the header is defined, then we emit the header, and then we simply append all the documents
 * to be aggregated into the resulting document.
 * </p>
 *
 * @author mcwarman
 *
 */
@XStreamAlias("csv-aggregator")
@ComponentProfile(summary = "Aggregate messages into a CSV, optionally prefixing a header", since = "3.9.3.1",
    tag = "csv,aggregator")
public class CsvAggregator extends CsvAggregating {

  @Override
  protected boolean forceColumns() {
    return false;
  }
}
