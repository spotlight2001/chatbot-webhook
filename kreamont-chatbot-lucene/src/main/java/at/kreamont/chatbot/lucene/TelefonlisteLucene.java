package at.kreamont.chatbot.lucene;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.RAMDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelefonlisteLucene {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelefonlisteLucene.class.getName());

	private static final String PHONE = "phone";
	private static final String NAME = "name";

	private Analyzer analyzer = new StandardAnalyzer();

	private RAMDirectory index;

	public void index(String csv) {
		index = new RAMDirectory();

		// Make an writer to create the index
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		try {
			IndexWriter writer = new IndexWriter(index, indexWriterConfig);

			String[] rows = csv.split("\r?\n");
			LOGGER.info(String.format("csv has '%s' rows", rows.length));
			for (int i = 2; i < rows.length; i++) { // skip first line
				String row = rows[i];
				String[] fields = row.split(";");

				if (fields.length < 21) {
					continue;
				}

				String kidFirstname = fields[1];
				String kidLastname = fields[0];

				String phone = fields[3];

				String motherFirstname = fields[4];
				String motherLastname = fields[5];
				String motherMobile = fields[6];

				String fatherFirstname = fields[7];
				String fatherLastname = fields[8];
				String fatherMobile = fields[9];

				boolean hasKid = StringUtils.isNotEmpty(kidFirstname) && StringUtils.isNotEmpty(kidLastname);
				boolean hasMother = StringUtils.isNotEmpty(motherFirstname) && StringUtils.isNotEmpty(motherLastname);
				boolean hasFather = StringUtils.isNotEmpty(fatherFirstname) && StringUtils.isNotEmpty(fatherLastname);

				if (hasKid) {
					Document kid = new Document();
					kid.add(new TextField(NAME, kidFirstname + " " + kidLastname, Store.YES));
					if (hasMother) {
						kid.add(new TextField("mother", motherFirstname + " " + motherLastname, Store.YES));
						kid.add(new TextField("mother.phone", StringUtils.defaultString(motherMobile, phone),
								Store.YES));
					}
					if (hasFather) {
						kid.add(new TextField("father", fatherFirstname + " " + fatherLastname, Store.YES));
						kid.add(new TextField("father.phone", StringUtils.defaultString(fatherMobile, phone),
								Store.YES));
					}
					writer.addDocument(kid);
				}

				if (hasMother) {
					Document mother = new Document();
					mother.add(new TextField(NAME, motherFirstname + " " + motherLastname, Store.YES));
					mother.add(new TextField(PHONE, StringUtils.defaultString(motherMobile, phone), Store.YES));
					writer.addDocument(mother);
				}

				if (hasFather) {
					Document father = new Document();
					father.add(new TextField(NAME, fatherFirstname + " " + fatherLastname, Store.YES));
					father.add(new TextField(PHONE, StringUtils.defaultString(fatherMobile, phone), Store.YES));
					writer.addDocument(father);
				}

				LOGGER.debug(String.format("csv row has '%s' values", fields.length));
				if (fields.length < 21) {
					continue;
				}
			}
			writer.close();
		} catch (IOException e) {
			// cant recover so rethrow
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unused")
	public String getTelephoneNumberAnswer(String firstname, String lastname) {

		boolean hasFirstname = StringUtils.isNotBlank(firstname);
		boolean hasLastname = StringUtils.isNotBlank(lastname);
		
		if (! hasFirstname && ! hasLastname) {
			return "Leider hab ich den Namen nicht mitbekommen, versuchs nochmal.";
		}
		
		StringBuilder fullname = new StringBuilder();
		if (hasFirstname) {
			fullname.append(firstname).append("~");
		}
		if (hasFirstname && hasLastname) {
			fullname.append(" ");
		}
		if (hasLastname) {
			fullname.append(lastname).append("~");
		}

		try {
			DirectoryReader reader = DirectoryReader.open(index);
			IndexSearcher searcher = new IndexSearcher(reader);
			Query query = new QueryParser(NAME, analyzer).parse(fullname.toString());
			TopDocs docs = searcher.search(query, 1);
			for (int i = 0; i < docs.scoreDocs.length; i++) {
				ScoreDoc result = docs.scoreDocs[i];
				int docId = result.doc;
				Document doc = searcher.doc(docId);
				StringBuilder buf = new StringBuilder();
				buf.append("Ich hab für Dich ").append(doc.get(NAME)).append(" gefunden. ");
				String phone = doc.get(PHONE);
				if (StringUtils.isEmpty(phone)) {
					// kind hat kein telefon -> muss daten von eltern holen
					String mother = doc.get("mother");
					String motherPhone = doc.get("mother.phone");
					boolean hasMother = mother != null && motherPhone != null;
					if (hasMother) {
						buf.append("Mama: ").append(mother).append(", ").append(motherPhone).append(". ");
					}

					String father = doc.get("father");
					String fatherPhone = doc.get("father.phone");
					boolean hasFather = father != null && fatherPhone != null;
					if (hasFather) {
						buf.append("Papa: ").append(father).append(", ").append(fatherPhone).append(". ");
					}
				} else {
					buf.append("Nummer: ").append(phone);
				}
				return buf.toString();
			}
		} catch (IOException | ParseException e) {
			throw new RuntimeException(e);
		}
		return "Jemanden mit Namen '" + fullname + "' hab ich leider nicht gefunden.";
	}
}