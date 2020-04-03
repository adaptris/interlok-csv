package com.adaptris.csv.aggregator;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregatorImpl;

import java.util.Collection;
import java.util.List;

import com.adaptris.core.util.ExceptionHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Attempts to aggregate messages into a CSV file.
 *
 * This aggregator attempts to validate the CSV by making use of the
 * Apache Commons CSV library. If a header is set then it defines how
 * many columns each row should have, otherwise the first record in
 * the first message dictates this.
 *
 * Each record is parsed using the Commons CSV Parser, which will
 * (hopefully) ensure that the record is valid CSV.
 *
 * @author ashley
 */
@XStreamAlias("csv-validating-aggregator")
@ComponentProfile(summary = "Aggregate messages into a CSV, optionally prefixing a header",
		since = "3.10.0",
		tag = "csv,aggregator,validate")
public class CsvValidatingAggregator extends CsvAggregating
{
	public CsvValidatingAggregator withHeader(String s) {
		header = s;
		return this;
	}

	public void setForceColumns(Boolean forceColumns)
	{
		super.forceColumns = forceColumns;
	}

	public Boolean getForceColumns()
	{
		return forceColumns();
	}
}
