package com.adaptris.csv.aggregator;

import com.adaptris.annotation.ComponentProfile;
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
public class CsvValidatingAggregator extends MessageAggregatorImpl
{
	private static final transient Logger log = LoggerFactory.getLogger(CsvValidatingAggregator.class);

	@InputFieldHint(expression = true)
	@Getter
	@Setter
	private String header;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void joinMessage(AdaptrisMessage message, Collection<AdaptrisMessage> messages) throws CoreException
	{
		// TODO Allow the format to be set from the UI
		try (CSVPrinter csv = new CSVPrinter(message.getWriter(), CSVFormat.DEFAULT))
		{
			int columns = -1;
			if (!StringUtils.isEmpty(header))
			{
				CSVParser parser = CSVParser.parse(header, CSVFormat.DEFAULT);
				List<CSVRecord> records = parser.getRecords();
				if (records.size() != 1)
				{
					// Maybe throw an exception?
					log.warn("CSV header has more than 1 row!");
				}
				columns = records.get(0).size();
				csv.printRecords(records);
			}

			for (AdaptrisMessage m : messages)
			{
				CSVParser parser = CSVParser.parse(m.getReader(), CSVFormat.DEFAULT);
				for (CSVRecord record : parser.getRecords())
				{
					if (columns < 0)
					{
						// there wasn't a header, so first record sets the number of columns
						columns = record.size();
					}
					if (record.size() == columns)
					{
						csv.printRecord(record);
					}
					else
					{
						// Maybe throw an exception?
						log.warn("Message {} Row {} has {} columns; expected {}", message.getUniqueId(), record.getRecordNumber(), record.size(), columns);
					}
				}
			}
		}
		catch (Exception e)
		{
			log.error("Could not aggregate CSV messages", e);
			throw ExceptionHelper.wrapCoreException(e);
		}
	}

	public CsvValidatingAggregator withHeader(String s) {
		header = s;
		return this;
	}
}
