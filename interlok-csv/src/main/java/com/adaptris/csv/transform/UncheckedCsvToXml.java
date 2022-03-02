package com.adaptris.csv.transform;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ServiceException;
import com.adaptris.csv.OrderedCsvMapReader;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.NoArgsConstructor;

/**
 * Unchecked CSV to XML using {@code net.sf.supercsv:super-csv}.
 *
 * <p>
 * This service does not attempt to make parsing decisions, so CSV files that have differing number
 * of columns on each line would be perfectly acceptable.
 * </p>
 * <p>
 * For example, given an input document :
 *
 * <pre>
 * {@code
HEADER,19052017
PRODUCT-CODE,BARCODE,DP,PROMPRICE,DISCOUNT,INSTOCK,REPORT,REPORT-DATE,SOURCE
XXXXXXXX,1234567890123,2.75,0.00,5.00,2,,,GSL
TRAILER,4
   }
 * </pre>
 *
 * Then the output would be
 *
 * <pre>
 * {@code
  <csv-xml>
   <record>
      <csv-field-1>HEADER<csv-field-1>
      <csv-field-2>19052017</csv-field-2>
   </record>
   <record>
      <csv-field-1>PRODUCT-CODE<csv-field-1>
      <csv-field-2>BARCODE</csv-field-2>
      <csv-field-3>DP</csv-field-3>
      <csv-field-4>PROMPRICE</csv-field-4>
      <csv-field-5>DISCOUNT</csv-field-5>
      <csv-field-6>INSTOCK</csv-field-6>
      <csv-field-7>REPORT</csv-field-7>
      <csv-field-8>REPORT-DATE</csv-field-8>
      <csv-field-9>SOURCE</csv-field-9>
   </record>
   <record>
      <csv-field-1>XXXXXXXX<csv-field-1>
      <csv-field-2>1234567890123</csv-field-2>
      <csv-field-3>,2.75</csv-field-3>
      <csv-field-4>0.00</csv-field-4>
      <csv-field-5>5.00</csv-field-5>
      <csv-field-6>2</csv-field-6>
      <csv-field-7></csv-field-7>
      <csv-field-8></csv-field-8>
      <csv-field-9>GSL</csv-field-9>
   </record>
   <record>
      <csv-field-1>TRAILER<csv-field-1>
      <csv-field-2>4</csv-field-2>
   </record>
  </csv-xml>
}
 * </pre>
 * </p>
 * <p>
 * Because there is no verification that the CSV columns matchup; if the message type isn't a CSV
 * (e.g. it's JSON or XML), then it will still be marked up into XML, so take that into account when
 * using this as part of a workflow. Behaviour with rogue messages may not be as you expect.
 * </p>
 *
 * @config unchecked-csv-to-xml-transform
 */
@XStreamAlias("unchecked-csv-to-xml-transform")
@AdapterComponent
@ComponentProfile(summary = "Easily transform a document from CSV to XML",
    tag = "service,transform,csv,xml", since = "3.11.0")
@DisplayOrder(order = {"format", "outputMessageEncoding", "stripIllegalXmlChars"})
@NoArgsConstructor
public class UncheckedCsvToXml extends CsvToXmlServiceImpl {


  @Override
  protected Document transform(AdaptrisMessage msg) throws ServiceException {
    Document doc = null;
    try (Reader in = msg.getReader();
        OrderedCsvMapReader csvReader =
            new OrderedCsvMapReader(in, buildPreferences());) {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      Element root = doc.createElement(XML_ROOT_ELEMENT);
      doc.appendChild(root);
      int recordCount = 1;
      for (List<String> record; (record = csvReader.readNext()) != null;) {
        Element recordElement = addNewElement(doc, root, CSV_RECORD_NAME);
        if (includeLineNumberAttribute()) {
          recordElement.setAttribute("line", String.valueOf(recordCount));
        }
        // Create the sub doc here.
        List<Element> csvFields = createFields(doc, record);
        for (Element field : csvFields) {
          recordElement.appendChild(field);
        }
        recordCount++;
      }
    }
    catch (Exception e) {
      throw new ServiceException("Failed to convert CSV to XML", e);
    }
    return doc;
  }


  private static final String createElementName(int count) {
    return CSV_FIELD_NAME + "-" + (count + 1);
  }

  private List<Element> createFields(Document doc, List<String> record) {
    List<Element> result = new ArrayList<>();
    for (int i = 0; i < record.size(); i++) {
      Element element = doc.createElement(createElementName(i));
      element.appendChild(createTextNode(doc, record.get(i)));
      result.add(element);
    }
    return result;
  }

}
