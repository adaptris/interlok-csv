package com.adaptris.csv.aggregator;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.services.aggregator.MessageAggregatorImpl;
import com.adaptris.core.util.ExceptionHelper;
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

import java.util.Collection;
import java.util.List;

public class CsvAggregating extends MessageAggregatorImpl
{
	private static final transient Logger log = LoggerFactory.getLogger(CsvValidatingAggregator.class);

	@InputFieldHint(expression = true)
	@Getter
	@Setter
	protected String header;

	@InputFieldHint(friendly = "Ensure number of columns in each row match the header")
	@InputFieldDefault(value = "true")
	@AdvancedConfig
	protected Boolean forceColumns;

	/**
	 * {@inheritDoc}.
	 */
	@Override
	public void joinMessage(AdaptrisMessage message, Collection<AdaptrisMessage> messages) throws CoreException
	{
		String header = message.resolve(this.header);
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
				List<CSVRecord> records = parser.getRecords();
				if (forceColumns())
				{
					for (CSVRecord record : records)
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
				else
				{
					csv.printRecords(records);
				}
			}
		}
		catch (Exception e)
		{
			log.error("Could not aggregate CSV messages", e);
			throw ExceptionHelper.wrapCoreException(e);
		}
	}

	protected boolean forceColumns()
	{
		return ObjectUtils.defaultIfNull(forceColumns, true);
	}
}
