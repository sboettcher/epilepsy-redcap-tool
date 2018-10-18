package epilepsy.redcap;

import com.opencsv.bean.CsvToBeanBuilder;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

import static epilepsy.util.Statics.loglvl;

public class DictionaryLoader {
  private static final Logger LOGGER = Logger.getLogger( DictionaryLoader.class.getName() );
  static {LOGGER.setLevel(loglvl);}


  public static List<DictionaryEntry> readDictionary(Reader reader) {
    List<DictionaryEntry> entries = new CsvToBeanBuilder(reader).withType(DictionaryEntry.class).build().parse();
    LOGGER.fine(String.format("Read %d entries", entries.size()));
    return entries;
  }

  public static List<DictionaryEntry> readFromFile(File file) throws FileNotFoundException {
    LOGGER.fine("Reading data dictionary from file " + file.getPath());
    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
    return readDictionary(br);
  }
  public static List<DictionaryEntry> readFromResource(String res) {
    LOGGER.fine("Reading data dictionary from resource " + res);
    BufferedReader br = new BufferedReader(new InputStreamReader(DictionaryLoader.class.getResourceAsStream(res)));
    return readDictionary(br);
  }
}
